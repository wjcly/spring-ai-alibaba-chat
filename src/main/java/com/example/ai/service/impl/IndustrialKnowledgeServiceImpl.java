package com.example.ai.service.impl;

import com.example.ai.service.IndustrialKnowledgeService;
import com.example.ai.service.RagService;
import com.example.ai.service.RagService.DocumentImportRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 工业场景知识库初始化服务实现
 */
@Service
public class IndustrialKnowledgeServiceImpl implements IndustrialKnowledgeService, CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(IndustrialKnowledgeServiceImpl.class);

    private final RagService ragService;

    public IndustrialKnowledgeServiceImpl(RagService ragService) {
        this.ragService = ragService;
    }

    @Override
    public void run(String... args) {
        // 可选：启动时自动初始化知识库
        // initConstructionSafetyKnowledge();
        // initIndustrialStandardKnowledge();
        logger.info("工业知识库服务已启动");
    }

    @Override
    public void initConstructionSafetyKnowledge() {
        logger.info("初始化工地安全知识库...");

        // 安全帽使用规范
        addSafetyRegulation(
                "安全帽使用规范",
                "1. 进入施工现场必须正确佩戴安全帽\n" +
                "2. 安全帽应符合 GB 2811-2019 标准\n" +
                "3. 安全帽佩戴要求：\n" +
                "   - 帽衬与帽壳间距保持在 25-50mm\n" +
                "   - 系好下颏带，松紧适度\n" +
                "   - 不得将安全帽当坐垫使用\n" +
                "4. 安全帽使用期限：塑料帽不超过 2.5 年，玻璃钢帽不超过 3.5 年\n" +
                "5. 发现裂纹、凹陷、破损等情况应立即更换",
                "安全帽", "个人防护", "施工现场");

        // 高处作业安全规范
        addSafetyRegulation(
                "高处作业安全规范",
                "1. 高处作业定义：坠落高度基准面 2m 及以上\n" +
                "2. 高处作业分级：\n" +
                "   - 一级：2-5m\n" +
                "   - 二级：5-15m\n" +
                "   - 三级：15-30m\n" +
                "   - 特级：30m 以上\n" +
                "3. 安全防护措施：\n" +
                "   - 正确佩戴安全带，高挂低用\n" +
                "   - 设置防护栏杆、安全网\n" +
                "   - 使用合格的操作平台\n" +
                "4. 禁止行为：\n" +
                "   - 酒后作业\n" +
                "   - 恶劣天气作业（6 级以上大风、暴雨、大雾等）\n" +
                "   - 无防护措施作业",
                "高处作业", "安全带", "防护网");

        // 脚手架安全规范
        addSafetyRegulation(
                "脚手架安全规范",
                "1. 脚手架搭设要求：\n" +
                "   - 基础坚实平整，设置垫板和底座\n" +
                "   - 立杆间距不大于 1.8m，步距不大于 1.8m\n" +
                "   - 设置纵横向扫地杆\n" +
                "   - 按规范设置连墙件\n" +
                "2. 验收要求：\n" +
                "   - 搭设完成后必须经验收合格方可使用\n" +
                "   - 验收合格牌应悬挂在明显位置\n" +
                "3. 使用要求：\n" +
                "   - 严禁超载使用\n" +
                "   - 严禁拆除主节点处的纵横向水平杆\n" +
                "   - 定期检查维护",
                "脚手架", "高处作业", "施工安全");

        // 临时用电安全规范
        addSafetyRegulation(
                "施工现场临时用电安全规范",
                "1. 临时用电组织设计：\n" +
                "   - 编制用电组织设计\n" +
                "   - 经审批后实施\n" +
                "2. 三级配电两级保护：\n" +
                "   - 总配电箱→分配电箱→开关箱\n" +
                "   - 总漏保和末级漏保两级保护\n" +
                "3. TN-S 接零保护系统：\n" +
                "   - 工作零线与保护零线分开\n" +
                "   - 保护零线不得装设开关或熔断器\n" +
                "4. 一机一闸一漏一箱：\n" +
                "   - 每台用电设备必须有专用开关箱\n" +
                "   - 严禁同一个开关电器直接控制两台及以上设备",
                "临时用电", "配电箱", "漏电保护");

        // 起重机械安全规范
        addSafetyRegulation(
                "起重机械安全规范",
                "1. 起重机械使用要求：\n" +
                "   - 必须经检验合格并登记\n" +
                "   - 操作人员持证上岗\n" +
                "   - 定期检验和维护保养\n" +
                "2. 十不吊原则：\n" +
                "   - 指挥信号不明不吊\n" +
                "   - 超负荷不吊\n" +
                "   - 工件紧固不牢不吊\n" +
                "   - 吊物上有人不吊\n" +
                "   - 安全装置不灵不吊\n" +
                "   - 工件埋在地下不吊\n" +
                "   - 光线阴暗看不清不吊\n" +
                "   - 棱角物件无防护措施不吊\n" +
                "   - 斜拉工件不吊\n" +
                "   - 钢水包过满不吊\n" +
                "3. 作业环境：\n" +
                "   - 设置警戒区域\n" +
                "   - 专人监护",
                "起重机械", "塔吊", "十不吊");

        logger.info("工地安全知识库初始化完成");
    }

    @Override
    public void initIndustrialStandardKnowledge() {
        logger.info("初始化工业规范知识库...");

        // 安全生产法要点
        addSafetyRegulation(
                "安全生产法核心要点",
                "1. 生产经营单位主体责任：\n" +
                "   - 建立健全安全生产责任制\n" +
                "   - 保证安全生产投入\n" +
                "   - 开展安全生产教育培训\n" +
                "   - 隐患排查治理\n" +
                "2. 从业人员权利义务：\n" +
                "   - 有权了解作业场所危险因素\n" +
                "   - 有权拒绝违章指挥\n" +
                "   - 发现直接危及人身安全的紧急情况时，有权停止作业\n" +
                "   - 必须遵守安全生产规章制度和操作规程\n" +
                "3. 法律责任：\n" +
                "   - 发生生产安全事故的，依法追究刑事责任\n" +
                "   - 对责任单位处以罚款、停产停业整顿等处罚",
                "安全生产法", "法律法规", "主体责任");

        // 危险作业管理
        addOperationProcedure(
                "危险作业审批流程",
                "1. 危险作业范围：\n" +
                "   - 动火作业\n" +
                "   - 受限空间作业\n" +
                "   - 高处作业\n" +
                "   - 吊装作业\n" +
                "   - 临时用电作业\n" +
                "   - 动土作业\n" +
                "   - 断路作业\n" +
                "2. 审批流程：\n" +
                "   - 作业单位提出申请\n" +
                "   - 安全管理部门审核\n" +
                "   - 相关负责人审批\n" +
                "   - 现场安全措施确认\n" +
                "   - 签发作业许可证\n" +
                "3. 作业过程管理：\n" +
                "   - 专人监护\n" +
                "   - 定时检测\n" +
                "   - 异常情况立即停止作业",
                "危险作业", "审批流程", "作业许可");

        // 事故报告流程
        addOperationProcedure(
                "事故报告与应急处置流程",
                "1. 事故报告：\n" +
                "   - 现场人员立即报告负责人\n" +
                "   - 单位负责人 1 小时内向主管部门报告\n" +
                "   - 报告内容：时间、地点、伤亡情况、初步原因\n" +
                "2. 应急处置：\n" +
                "   - 立即启动应急预案\n" +
                "   - 组织抢救伤员\n" +
                "   - 保护事故现场\n" +
                "   - 疏散无关人员\n" +
                "   - 防止事故扩大\n" +
                "3. 事故调查：\n" +
                "   - 成立调查组\n" +
                "   - 查明事故原因\n" +
                "   - 认定事故责任\n" +
                "   - 提出整改措施",
                "事故报告", "应急处置", "事故调查");

        logger.info("工业规范知识库初始化完成");
    }

    @Override
    public void addSafetyRegulation(String title, String content, String... keywords) {
        ragService.addDocument(
                content,
                title,
                "规范",
                "工地安全",
                List.of(keywords));
        logger.info("添加安全规范：{}", title);
    }

    @Override
    public void addOperationProcedure(String title, String content, String... keywords) {
        ragService.addDocument(
                content,
                title,
                "流程",
                "工业规范",
                List.of(keywords));
        logger.info("添加操作流程：{}", title);
    }

    @Override
    public void addAccidentCase(String title, String content, String lessons) {
        String fullContent = "【案例描述】\n" + content + "\n\n【教训总结】\n" + lessons;
        ragService.addDocument(
                fullContent,
                title,
                "案例",
                "工地安全",
                List.of("事故案例", "教训"));
        logger.info("添加事故案例：{}", title);
    }

    @Override
    public void addInspectionItems(String category, String items) {
        ragService.addDocument(
                items,
                category + "检查项",
                "检查",
                "工地安全",
                List.of("安全检查", category));
        logger.info("添加检查项：{}", category);
    }

    /**
     * 批量导入示例数据
     */
    public void bulkImportSampleData() {
        List<DocumentImportRequest> documents = new ArrayList<>();

        // 工地安全类
        documents.add(new DocumentImportRequest(
                "进入施工现场必须穿戴好个人防护用品，包括安全帽、安全鞋、反光背心等。",
                "施工现场个人防护要求",
                "规范",
                "工地安全",
                List.of("个人防护", "安全帽", "施工现场")));

        documents.add(new DocumentImportRequest(
                "深基坑开挖应遵循'开槽支撑、先撑后挖、分层开挖、严禁超挖'的原则。",
                "深基坑开挖安全要求",
                "规范",
                "工地安全",
                List.of("深基坑", "开挖", "支撑")));

        // 工业规范类
        documents.add(new DocumentImportRequest(
                "特种作业人员必须经专门的安全作业培训，取得特种作业操作资格证书，方可上岗作业。",
                "特种作业人员管理要求",
                "规范",
                "工业规范",
                List.of("特种作业", "培训", "持证上岗")));

        ragService.bulkImportDocuments(documents);
    }
}
