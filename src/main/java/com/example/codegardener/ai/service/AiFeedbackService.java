package com.example.codegardener.ai.service;

import com.example.codegardener.post.domain.Post;
import com.example.codegardener.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiFeedbackService {

    private final PostRepository postRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${ai.mock.enabled:true}")
    private boolean mockEnabled;

    @Value("${ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${ai.openai.temperature:0.7}")
    private double temperature;

    @Value("${ai.openai.max-tokens:800}")
    private int maxTokens;

    // 게시글 기반 AI 피드백 생성
    public String generateTextForPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다."));

        String prompt = buildPrompt(post);

        if (mockEnabled) {
            log.info("[AI] Mock 모드 활성화 - 실제 API 호출 없이 텍스트 반환");
            return mockResponse();
        }

        return callOpenAi(prompt);
    }

    // 프롬포트
    private String buildPrompt(Post p) {
        return """
        당신은 숙련된 코드 리뷰어입니다.
        아래 코드를 읽고 자연스러운 한국어 문장으로 피드백을 작성하세요.
        구조, 가독성, 논리, 개선점, 칭찬할 부분 등을 자유롭게 언급해 주세요.
        형식 제약 없이 서술체로 써 주세요.

        [게시물 요약 / 의도]
        %s

        [피드백 받고 싶은 부분]
        %s

        [코드]
        %s
        """.formatted(
                safe(p.getSummary()),
                safe(p.getContent()),
                safe(p.getCode())
        );
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }

    // OpenAI API 호출
    private String callOpenAi(String prompt) {
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API 키가 설정되지 않았습니다. (ai.openai.api-key)");
        }

        try {
            WebClient client = webClientBuilder
                    .baseUrl("https://api.openai.com/v1")
                    .defaultHeader("Authorization", "Bearer " + openAiApiKey)
                    .build();

            String body = """
                {
                  "model": "%s",
                  "messages": [
                    {"role": "system", "content": "You are a professional Korean code reviewer."},
                    {"role": "user", "content": "%s"}
                  ],
                  "temperature": %.2f,
                  "max_tokens": %d
                }
                """.formatted(model, escape(prompt), temperature, maxTokens);

            String response = client.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isBlank()) {
                return "AI 응답이 비어 있습니다.";
            }

            // JSON 파싱 (JSONArray length()로 변경)
            JSONObject json = new JSONObject(response);
            JSONArray choices = json.getJSONArray("choices");
            if (choices.isEmpty()) return "AI 응답이 없습니다.";

            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            String content = message.optString("content", "AI 응답을 파싱하지 못했습니다.");

            log.info("[AI] Response received successfully for model {}", model);
            return content.trim();

        } catch (Exception e) {
            log.error("[AI] OpenAI 호출 실패: {}", e.getMessage(), e);
            return "AI 피드백 생성 실패: " + e.getMessage();
        }
    }

    private String escape(String text) {
        return text.replace("\"", "\\\"").replace("\n", "\\n");
    }

    // 테스트용 Mock 응답
    private String mockResponse() {
        return """
        코드의 구조가 전반적으로 명확하고 일관성 있습니다.
        변수명 또한 직관적이며 가독성이 좋습니다.
        다만 예외 처리 부분에서 조금 더 구체적인 오류 메시지를 제공하면 좋겠습니다.
        """;
    }
}