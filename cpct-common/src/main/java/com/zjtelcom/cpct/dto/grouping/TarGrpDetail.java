package com.zjtelcom.cpct.dto.grouping;

import com.zjtelcom.cpct.domain.grouping.TarGrpDetailDO;
import lombok.Data;
import java.util.List;

@Data
public class TarGrpDetail extends TarGrpDetailDO {

    private Long tarGrpId; //目标分群标识
    private String tarGrpName;//目标分群名称
    private String tarGrpDesc;//目标分群的详细描述信息
    private String tarGrpType;//目标分群类型,1000	客户 2000	产品实例 3000	销售品 4000	营销资源 5000	礼包


}