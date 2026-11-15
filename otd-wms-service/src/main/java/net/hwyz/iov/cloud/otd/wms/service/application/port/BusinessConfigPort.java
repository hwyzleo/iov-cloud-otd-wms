package net.hwyz.iov.cloud.otd.wms.service.application.port;

/**
 * 业务配置端口（应用层定义，基础设施从类型化配置提供）
 *
 * @author hwyz_leo
 */
public interface BusinessConfigPort {

    /**
     * 缺省仓库代码
     *
     * @return 缺省仓库代码；未配置时为空
     */
    String defaultWarehouseCode();
}
