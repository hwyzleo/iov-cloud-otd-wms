package net.hwyz.iov.cloud.otd.wms.service.infrastructure.config;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.wms.service.application.port.BusinessConfigPort;
import org.springframework.stereotype.Component;

/**
 * 业务配置端口实现
 *
 * @author hwyz_leo
 */
@Component
@RequiredArgsConstructor
public class BusinessConfigAdapter implements BusinessConfigPort {

    private final WmsBusinessProperties wmsBusinessProperties;

    @Override
    public String defaultWarehouseCode() {
        return wmsBusinessProperties.getDefaultWarehouse().getCode();
    }
}
