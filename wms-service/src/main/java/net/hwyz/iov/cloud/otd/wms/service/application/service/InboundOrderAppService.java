package net.hwyz.iov.cloud.otd.wms.service.application.service;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.util.StrUtil;
import net.hwyz.iov.cloud.otd.wms.api.contract.enums.WarehouseLevel;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao.InboundOrderDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InboundOrderPo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 入库单应用服务类
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InboundOrderAppService {

    private final InboundOrderDao inboundOrderDao;

    /**
     * 默认的前置仓库代码
     */
    @Value("${biz.default-warehouse.pdc}")
    private String defaultPdcWarehouseCode;

    /**
     * 查询入库单
     *
     * @param orderNum       单号
     * @param vin            车架号
     * @param warehouseLevel 仓库体系层级
     * @param warehouseCode  仓库代码
     * @param beginTime      开始时间
     * @param endTime        结束时间
     * @return 入库单列表
     */
    public List<InboundOrderPo> search(String orderNum, String vin, String warehouseLevel, String warehouseCode,
                                       Date beginTime, Date endTime) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderNum", orderNum);
        map.put("vin", vin);
        map.put("warehouseLevel", warehouseLevel);
        map.put("warehouseCode", warehouseCode);
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        return inboundOrderDao.selectPoByMap(map);
    }

    /**
     * 检查入库单是否唯一
     *
     * @param inboundOrderId 入库单ID
     * @param orderNum       入库单号
     * @return 结果
     */
    public Boolean checkOrderNumUnique(Long inboundOrderId, String orderNum) {
        if (ObjUtil.isNull(inboundOrderId)) {
            inboundOrderId = -1L;
        }
        InboundOrderPo inboundOrderPo = getWarehouseByOrderNum(orderNum);
        return !ObjUtil.isNotNull(inboundOrderPo) || inboundOrderPo.getId().longValue() == inboundOrderId.longValue();
    }

    /**
     * 根据主键ID获取入库单
     *
     * @param id 主键ID
     * @return 入库单
     */
    public InboundOrderPo getInboundOrderById(Long id) {
        return inboundOrderDao.selectPoById(id);
    }

    /**
     * 根据入库单号获取入库单
     *
     * @param orderNum 入库单号
     * @return 入库单
     */
    public InboundOrderPo getWarehouseByOrderNum(String orderNum) {
        return inboundOrderDao.selectPoByOrderNum(orderNum);
    }

    /**
     * 新增入库单
     *
     * @param inboundOrder 入库单
     * @return 结果
     */
    public int createInboundOrder(InboundOrderPo inboundOrder) {
        inboundOrder.setOrderNum(generateOrderNum());
        if (StrUtil.isBlank(inboundOrder.getWarehouseCode())) {
            switch (WarehouseLevel.valOf(inboundOrder.getWarehouseLevel())) {
                case PDC -> inboundOrder.setWarehouseCode(defaultPdcWarehouseCode);
                default -> {
                    logger.warn("未指定仓库体系层级，默认为前置库");
                    inboundOrder.setWarehouseCode(defaultPdcWarehouseCode);
                }
            }
        }
        return inboundOrderDao.insertPo(inboundOrder);
    }

    /**
     * 修改入库单
     *
     * @param inboundOrder 入库单
     * @return 结果
     */
    public int modifyInboundOrder(InboundOrderPo inboundOrder) {
        return inboundOrderDao.updatePo(inboundOrder);
    }

    /**
     * 批量删除入库单
     *
     * @param ids 入库单ID数组
     * @return 结果
     */
    public int deleteInboundOrderByIds(Long[] ids) {
        return inboundOrderDao.batchPhysicalDeletePo(ids);
    }

    /**
     * 生成预入库单号
     *
     * @return 预入库单号
     */
    private String generateOrderNum() {
        return "IO" + LocalDateTimeUtil.format(LocalDateTimeUtil.now(), "yyyyMMddHHmmssSSS") +
                RandomUtil.randomNumbers(3);
    }

}
