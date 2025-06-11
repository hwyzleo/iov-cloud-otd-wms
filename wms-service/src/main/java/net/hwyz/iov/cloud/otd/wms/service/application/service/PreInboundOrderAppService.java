package net.hwyz.iov.cloud.otd.wms.service.application.service;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.util.StrUtil;
import net.hwyz.iov.cloud.otd.wms.api.contract.enums.WarehouseLevel;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao.PreInboundOrderDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.PreInboundOrderPo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预入库单应用服务类
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreInboundOrderAppService {

    private final PreInboundOrderDao preInboundOrderDao;

    /**
     * 默认的前置仓库代码
     */
    @Value("${biz.default-warehouse.pdc}")
    private String defaultPdcWarehouseCode;

    /**
     * 查询预入库单
     *
     * @param orderNum       单号
     * @param vin            车架号
     * @param warehouseLevel 仓库体系层级
     * @param warehouseCode  仓库代码
     * @param beginTime      开始时间
     * @param endTime        结束时间
     * @return 预入库单列表
     */
    public List<PreInboundOrderPo> search(String orderNum, String vin, String warehouseLevel, String warehouseCode,
                                          Date beginTime, Date endTime) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderNum", orderNum);
        map.put("vin", vin);
        map.put("warehouseLevel", warehouseLevel);
        map.put("warehouseCode", warehouseCode);
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        return preInboundOrderDao.selectPoByMap(map);
    }

    /**
     * 检查预入库单是否唯一
     *
     * @param preInboundOrderId 预入库单ID
     * @param orderNum          预入库单号
     * @return 结果
     */
    public Boolean checkOrderNumUnique(Long preInboundOrderId, String orderNum) {
        if (ObjUtil.isNull(preInboundOrderId)) {
            preInboundOrderId = -1L;
        }
        PreInboundOrderPo preInboundOrderPo = getWarehouseByOrderNum(orderNum);
        return !ObjUtil.isNotNull(preInboundOrderPo) || preInboundOrderPo.getId().longValue() == preInboundOrderId.longValue();
    }

    /**
     * 根据主键ID获取预入库单
     *
     * @param id 主键ID
     * @return 预入库单
     */
    public PreInboundOrderPo getPreInboundOrderById(Long id) {
        return preInboundOrderDao.selectPoById(id);
    }

    /**
     * 根据预入库单号获取预入库单
     *
     * @param orderNum 预入库单号
     * @return 预入库单
     */
    public PreInboundOrderPo getWarehouseByOrderNum(String orderNum) {
        return preInboundOrderDao.selectPoByOrderNum(orderNum);
    }

    /**
     * 新增预入库单
     *
     * @param preInboundOrder 预入库单
     * @return 结果
     */
    public int createPreInboundOrder(PreInboundOrderPo preInboundOrder) {
        preInboundOrder.setOrderNum(generateOrderNum());
        if (StrUtil.isBlank(preInboundOrder.getWarehouseCode())) {
            switch (WarehouseLevel.valOf(preInboundOrder.getWarehouseLevel())) {
                case PDC -> preInboundOrder.setWarehouseCode(defaultPdcWarehouseCode);
                default -> {
                    logger.warn("未指定仓库体系层级，默认为前置库");
                    preInboundOrder.setWarehouseCode(defaultPdcWarehouseCode);
                }
            }
        }
        if (ObjUtil.isNull(preInboundOrder.getAudit())) {
            preInboundOrder.setAudit(false);
        }
        if (ObjUtil.isNull(preInboundOrder.getArrival())) {
            preInboundOrder.setArrival(false);
        }
        if (ObjUtil.isNull(preInboundOrder.getInbound())) {
            preInboundOrder.setInbound(false);
        }
        return preInboundOrderDao.insertPo(preInboundOrder);
    }

    /**
     * 修改预入库单
     *
     * @param preInboundOrder 预入库单
     * @return 结果
     */
    public int modifyPreInboundOrder(PreInboundOrderPo preInboundOrder) {
        return preInboundOrderDao.updatePo(preInboundOrder);
    }

    /**
     * 批量删除预入库单
     *
     * @param ids 预入库单ID数组
     * @return 结果
     */
    public int deletePreInboundOrderByIds(Long[] ids) {
        return preInboundOrderDao.batchPhysicalDeletePo(ids);
    }

    /**
     * 生成预入库单号
     *
     * @return 预入库单号
     */
    private String generateOrderNum() {
        return "PIO" + LocalDateTimeUtil.format(LocalDateTimeUtil.now(), "yyyyMMddHHmmssSSS") +
                RandomUtil.randomNumbers(3);
    }

}
