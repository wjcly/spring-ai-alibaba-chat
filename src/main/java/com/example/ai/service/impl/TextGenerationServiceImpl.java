package com.example.ai.service.impl;

import com.example.ai.service.TextGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文本生成服务实现
 */
@Service
public class TextGenerationServiceImpl implements TextGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(TextGenerationServiceImpl.class);

    private final ChatClient chatClient;

    public TextGenerationServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String generateWorkReport(Map<String, Object> workData, String reportType) {
        logger.info("生成工作报告：type={}", reportType);

        String prompt = buildWorkReportPrompt(workData, reportType);
        
        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    @Override
    public String generateSafetyReport(Map<String, Object> inspectionData, String scene) {
        logger.info("生成安全检查报告：scene={}", scene);

        String prompt = buildSafetyReportPrompt(inspectionData, scene);
        
        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    @Override
    public String generateMeetingMinutes(String meetingNotes, List<String> participants) {
        logger.info("生成会议纪要");

        String participantsStr = participants != null ? 
                String.join("、", participants) : "未提供";

        String prompt = String.format(
                "请根据以下会议记录生成规范的会议纪要：\n\n" +
                "参会人员：%s\n" +
                "会议记录：%s\n\n" +
                "要求：\n" +
                "1. 包含会议时间、地点、主题\n" +
                "2. 整理会议主要内容和决议\n" +
                "3. 列出待办事项和责任人\n" +
                "4. 格式规范、条理清晰",
                participantsStr, meetingNotes);

        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    @Override
    public String generateTechnicalDocument(String topic, List<String> outline, Map<String, String> requirements) {
        logger.info("生成技术文档：topic={}", topic);

        String outlineStr = outline != null ? 
                outline.stream().map(s -> "• " + s).collect(Collectors.joining("\n")) : "无";
        
        String requirementsStr = requirements != null ? 
                requirements.entrySet().stream()
                        .map(e -> e.getKey() + ": " + e.getValue())
                        .collect(Collectors.joining("\n")) : "无";

        String prompt = String.format(
                "请撰写以下技术文档：\n\n" +
                "【文档主题】\n%s\n\n" +
                "【文档大纲】\n%s\n\n" +
                "【要求】\n%s\n\n" +
                "请按照技术文档规范撰写，内容详实、结构清晰、语言专业。",
                topic, outlineStr, requirementsStr);

        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    @Override
    public String polishContent(String content, String style) {
        logger.info("润色内容：style={}", style);

        String stylePrompt;
        String styleLower = style.toLowerCase();
        if ("正式".equals(styleLower)) {
            stylePrompt = "请使用正式、规范的书面语言";
        } else if ("简洁".equals(styleLower)) {
            stylePrompt = "请精简内容，保留核心信息，语言简练";
        } else if ("详细".equals(styleLower)) {
            stylePrompt = "请扩充内容，增加细节和说明";
        } else if ("生动".equals(styleLower)) {
            stylePrompt = "请使用生动形象的语言，增加可读性";
        } else {
            stylePrompt = "请优化语言表达，使内容更流畅、专业";
        }

        String prompt = String.format(
                "请对以下内容进行润色：\n\n%s\n\n" +
                "要求：%s",
                content, stylePrompt);

        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    @Override
    public String expandContent(String brief, int targetLength) {
        logger.info("扩写内容：targetLength={}", targetLength);

        String prompt = String.format(
                "请对以下简要内容进行扩写，目标字数约%d字：\n\n" +
                "【原始内容】\n%s\n\n" +
                "要求：\n" +
                "1. 保留原始核心信息\n" +
                "2. 增加必要的背景、细节和说明\n" +
                "3. 结构完整、逻辑清晰\n" +
                "4. 语言流畅、专业",
                targetLength, brief);

        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    @Override
    public String generateNotice(String topic, List<String> keyPoints, String audience) {
        logger.info("生成通知：topic={}, audience={}", topic, audience);

        String keyPointsStr = keyPoints != null ? 
                keyPoints.stream().map(s -> "• " + s).collect(Collectors.joining("\n")) : "无";

        String prompt = String.format(
                "请撰写一份通知/公告：\n\n" +
                "【通知主题】\n%s\n\n" +
                "【要点】\n%s\n\n" +
                "【受众】\n%s\n\n" +
                "要求：格式规范、表达清晰、重点突出",
                topic, keyPointsStr, audience != null ? audience : "全体员工");

        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    @Override
    public String generateTrainingMaterial(String topic, String level, String duration) {
        logger.info("生成培训材料：topic={}, level={}, duration={}", topic, level, duration);

        String prompt = String.format(
                "请编写培训材料：\n\n" +
                "【培训主题】%s\n" +
                "【难度级别】%s\n" +
                "【培训时长】%s\n\n" +
                "要求：\n" +
                "1. 包含培训目标\n" +
                "2. 分章节组织内容\n" +
                "3. 每章包含知识点讲解和实例\n" +
                "4. 提供练习题或思考题\n" +
                "5. 难度适中，符合%s学员水平",
                topic, level, duration, level);

        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    /**
     * 构建工作报告提示词
     */
    private String buildWorkReportPrompt(Map<String, Object> workData, String reportType) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy 年 MM 月 dd 日"));
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下工作数据生成一份").append(reportType).append("：\n\n");
        prompt.append("【报告日期】").append(date).append("\n\n");

        if (workData.containsKey("completedWork")) {
            prompt.append("【已完成工作】\n").append(workData.get("completedWork")).append("\n\n");
        }
        if (workData.containsKey("ongoingWork")) {
            prompt.append("【进行中工作】\n").append(workData.get("ongoingWork")).append("\n\n");
        }
        if (workData.containsKey("problems")) {
            prompt.append("【存在问题】\n").append(workData.get("problems")).append("\n\n");
        }
        if (workData.containsKey("plan")) {
            prompt.append("【下一步计划】\n").append(workData.get("plan")).append("\n\n");
        }

        prompt.append("要求：\n");
        prompt.append("1. 结构清晰，分点叙述\n");
        prompt.append("2. 语言简洁、专业\n");
        prompt.append("3. 突出工作成果和亮点\n");
        prompt.append("4. 问题描述客观，提出解决方案\n");

        return prompt.toString();
    }

    /**
     * 构建安全检查报告提示词
     */
    private String buildSafetyReportPrompt(Map<String, Object> inspectionData, String scene) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy 年 MM 月 dd 日"));

        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下检查数据生成一份").append(scene).append("安全检查报告：\n\n");
        prompt.append("【检查日期】").append(date).append("\n\n");

        if (inspectionData.containsKey("inspectionArea")) {
            prompt.append("【检查区域】\n").append(inspectionData.get("inspectionArea")).append("\n\n");
        }
        if (inspectionData.containsKey("findings")) {
            prompt.append("【检查发现】\n").append(inspectionData.get("findings")).append("\n\n");
        }
        if (inspectionData.containsKey("hazards")) {
            prompt.append("【安全隐患】\n").append(inspectionData.get("hazards")).append("\n\n");
        }
        if (inspectionData.containsKey("rectifications")) {
            prompt.append("【整改要求】\n").append(inspectionData.get("rectifications")).append("\n\n");
        }

        prompt.append("要求：\n");
        prompt.append("1. 按照安全检查报告规范格式\n");
        prompt.append("2. 隐患描述具体，明确整改期限\n");
        prompt.append("3. 引用相关安全规范条款\n");
        prompt.append("4. 提出预防措施建议\n");

        return prompt.toString();
    }
}
