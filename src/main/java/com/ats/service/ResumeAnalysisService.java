package com.ats.service;

import com.ats.model.ResumeAnalysis;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.ats.model.Application;
import com.ats.model.ResumeAnalysisResult;
import com.ats.repository.ResumeAnalysisResultRepository;
import org.springframework.web.multipart.MultipartFile;
import com.ats.model.Job;
// import java.net.http.HttpClient;
// import java.net.http.HttpRequest;
// import java.net.http.HttpResponse;
// import java.net.URI;
// import java.io.IOException;
import java.util.ArrayList;


@Service
public class ResumeAnalysisService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.base-url}")
    private String url;

    @Autowired
    private ResumeAnalysisResultRepository resumeAnalysisResultRepository;

    public ResumeAnalysis analyzeResume(String resumeText)  {
        OpenAiService service = new OpenAiService(openaiApiKey);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", 
            "You are a resume analyzer. Extract location, skills, years of experience, and highest education from the resume text. " +
            "Respond in JSON format with fields: location (string), skills (array), yearsOfExperience (string), highestEducation (string)"));
        messages.add(new ChatMessage("user", resumeText));

        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .messages(messages)
            .model("gpt-3.5-turbo")
            .build();
        

        String response = service.createChatCompletion(request)
            .getChoices().get(0).getMessage().getContent();

            int startPos = response.indexOf("{");
            int endPos = response.lastIndexOf("}") + 1;
            String jsonResponse = response.substring(startPos, endPos);
            System.out.println(jsonResponse);

        ResumeAnalysis analysis = new ResumeAnalysis();
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);  // Use body() method
            List<Object> skillObjects = jsonObject.getJSONArray("skills").toList();
            List<String> skills = skillObjects.stream()
                .map(Object::toString)
                .toList();
            analysis.setSkills(skills);
            analysis.setYearsOfExperience(jsonObject.getString("yearsOfExperience"));
            analysis.setHighestEducation(jsonObject.getString("highestEducation"));

            analysis.setLocation(jsonObject.optString("location", "none"));
        } catch (JSONException err) {
            System.out.println(response);
            throw new RuntimeException("Failed to parse resume analysis: " + err.getMessage());
        }

        // Parse JSON response and create ResumeAnalysis object
        // For simplicity, using basic string parsing. In production, use proper JSON parsing
        
        // Basic parsing logic here
        return analysis;
    }

    public ResumeAnalysisResult getDetails(Application application) {
        return resumeAnalysisResultRepository.findByApplication(application);
    }

    public Job extractTextFromJobDescription(String jobDescriptionText) {
        OpenAiService service = new OpenAiService(openaiApiKey);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", 
            "You are a document analyzer. The document is a job description posted by an employer. Extract the job title, description and requirements from the  text. " +
            "Respond in JSON format with fields: title (string), description (string), requirements (string)"));
        messages.add(new ChatMessage("user", jobDescriptionText));

        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .messages(messages)
            .model("gpt-3.5-turbo")
            .build();
        

        String response = service.createChatCompletion(request)
            .getChoices().get(0).getMessage().getContent();

            int startPos = response.indexOf("{");
            int endPos = response.lastIndexOf("}") + 1;
            String jsonResponse = response.substring(startPos, endPos);
            System.out.println(jsonResponse);

            Job job = new Job();
            JSONObject jsonObject = new JSONObject(jsonResponse);
            job.setTitle(jsonObject.getString("title"));
            job.setDescription(jsonObject.getString("description"));
            job.setRequirements(jsonObject.getString("requirements"));
            return job;

    }
} 