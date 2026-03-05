package com.example.ai.service;

/**
 * 工业场景知识库初始化服务
 * 预置工地安全、工业规范等知识
 */
public interface IndustrialKnowledgeService {

    /**
     * 初始化工地安全知识库
     */
    void initConstructionSafetyKnowledge();

    /**
     * 初始化工业规范知识库
     */
    void initIndustrialStandardKnowledge();

    /**
     * 添加安全规范
     * @param title 规范标题
     * @param content 规范内容
     * @param keywords 关键词
     */
    void addSafetyRegulation(String title, String content, String... keywords);

    /**
     * 添加操作流程
     * @param title 流程标题
     * @param content 流程内容
     * @param keywords 关键词
     */
    void addOperationProcedure(String title, String content, String... keywords);

    /**
     * 添加事故案例
     * @param title 案例标题
     * @param content 案例内容
     * @param lessons 教训总结
     */
    void addAccidentCase(String title, String content, String lessons);

    /**
     * 添加工地检查项
     * @param category 检查类别
     * @param items 检查项列表
     */
    void addInspectionItems(String category, String items);
}
