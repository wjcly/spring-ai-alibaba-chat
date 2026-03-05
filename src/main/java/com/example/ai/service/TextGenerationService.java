package com.example.ai.service;

import java.util.List;
import java.util.Map;

/**
 * 文本生成服务接口
 * 支持报告生成、文档自动生成等能力
 */
public interface TextGenerationService {

    /**
     * 生成工作报告
     * @param workData 工作数据
     * @param reportType 报告类型 (日报/周报/月报)
     * @return 生成的报告
     */
    String generateWorkReport(Map<String, Object> workData, String reportType);

    /**
     * 生成安全检查报告
     * @param inspectionData 检查数据
     * @param scene 场景 (工地/工厂)
     * @return 生成的报告
     */
    String generateSafetyReport(Map<String, Object> inspectionData, String scene);

    /**
     * 生成会议纪要
     * @param meetingNotes 会议记录
     * @param participants 参会人员
     * @return 生成的纪要
     */
    String generateMeetingMinutes(String meetingNotes, List<String> participants);

    /**
     * 生成技术文档
     * @param topic 主题
     * @param outline 大纲
     * @param requirements 要求
     * @return 生成的文档
     */
    String generateTechnicalDocument(String topic, List<String> outline, Map<String, String> requirements);

    /**
     * 内容润色
     * @param content 原始内容
     * @param style 风格 (正式/简洁/详细)
     * @return 润色后的内容
     */
    String polishContent(String content, String style);

    /**
     * 内容扩写
     * @param brief 简要内容
     * @param targetLength 目标长度
     * @return 扩写后的内容
     */
    String expandContent(String brief, int targetLength);

    /**
     * 生成通知/公告
     * @param topic 主题
     * @param keyPoints 要点
     * @param audience 受众
     * @return 生成的通知
     */
    String generateNotice(String topic, List<String> keyPoints, String audience);

    /**
     * 生成培训材料
     * @param topic 培训主题
     * @param level 难度级别 (初级/中级/高级)
     * @param duration 培训时长
     * @return 生成的培训材料
     */
    String generateTrainingMaterial(String topic, String level, String duration);
}
