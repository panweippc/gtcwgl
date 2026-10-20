package com.jxc.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jxc.common.BusinessException;
import com.jxc.context.UserContext;
import com.jxc.module.system.entity.SysOperationLog;
import com.jxc.module.system.entity.SysUser;
import com.jxc.module.system.service.SysOperationLogService;
import com.jxc.module.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志切面
 * 拦截控制器写操作(POST/PUT/DELETE), 异步记录操作日志到数据库
 *
 * @author jxc
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect implements HandlerInterceptor {

    private final SysOperationLogService logService;
    private final SysUserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 模块名称中文映射表: Controller简单类名(去Controller后缀) → 中文模块名 */
    private static final Map<String, String> MODULE_MAP = new HashMap<>();
    static {
        MODULE_MAP.put("Warehouse", "仓库管理");
        MODULE_MAP.put("Product", "商品管理");
        MODULE_MAP.put("ProductCategory", "商品分类");
        MODULE_MAP.put("Customer", "客户管理");
        MODULE_MAP.put("CustomerCategory", "客户分类");
        MODULE_MAP.put("SysUser", "用户管理");
        MODULE_MAP.put("SysRole", "角色管理");
        MODULE_MAP.put("SysOperationLog", "操作日志");
        MODULE_MAP.put("SysMenu", "菜单管理");
        MODULE_MAP.put("Auth", "登录认证");
        MODULE_MAP.put("Supplier", "供应商管理");
        MODULE_MAP.put("SupplierCategory", "供应商分类");
        MODULE_MAP.put("Stats", "数据统计");
        MODULE_MAP.put("PurchaseOrder", "采购订单");
        MODULE_MAP.put("PurchaseReturnOrder", "采购退货");
        MODULE_MAP.put("SaleOrder", "销售订单");
        MODULE_MAP.put("SaleReturnOrder", "销售退货");
        MODULE_MAP.put("Stock", "库存管理");
        MODULE_MAP.put("StockWarningConfig", "库存预警配置");
        MODULE_MAP.put("StockTransfer", "库存调拨");
        MODULE_MAP.put("StockCheck", "库存盘点");
    }

    /**
     * 切入所有控制器方法
     */
    @Pointcut("execution(public * com.jxc.module..controller..*(..))")
    public void controllerPoint() {
    }

    /**
     * 环绕通知:记录操作日志
     * 仅记录写操作(POST/PUT/DELETE), GET查询不记录
     *
     * @param pjp 连接点
     * @return 方法返回值
     * @throws Throwable 方法异常
     */
    @Around("controllerPoint()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = getRequest();
        // 非HTTP请求或GET方法不记录日志
        if (request == null || "GET".equalsIgnoreCase(request.getMethod())) {
            return pjp.proceed();
        }
        // 排除操作日志控制器, 避免循环记录
        String uri = request.getRequestURI();
        if (uri != null && uri.contains("/system/log")) {
            return pjp.proceed();
        }

        long startTime = System.currentTimeMillis();
        SysOperationLog logEntity = new SysOperationLog();
        logEntity.setRequestMethod(request.getMethod());
        logEntity.setRequestUrl(uri);
        logEntity.setMethod(pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName());
        logEntity.setIp(getIpAddress(request));
        logEntity.setModule(extractModule(pjp, request));
        logEntity.setOperation(extractOperation(pjp.getSignature().getName(), logEntity.getModule()));
        logEntity.setRequestParam(serializeArgs(pjp.getArgs(), request));

        // 当前操作人
        Long userId = UserContext.getCurrentUserId();
        if (userId != null) {
            logEntity.setUserId(userId);
            try {
                SysUser user = userService.getById(userId);
                if (user != null) {
                    logEntity.setUsername(user.getUsername());
                }
            } catch (Exception ignored) {
                // 忽略用户查询异常
            }
        }

        Object result;
        try {
            result = pjp.proceed();
            logEntity.setStatus(1);
        } catch (BusinessException e) {
            // 业务校验拒绝(如"有采购记录不可删除")属于正常业务结果,不属于系统异常
            // 记录为成功,不记录错误信息,异常继续抛出由全局异常处理器返回业务提示
            logEntity.setStatus(1);
            throw e;
        } catch (Throwable e) {
            logEntity.setStatus(0);
            String errMsg = e.getMessage();
            if (errMsg != null && errMsg.length() > 2000) {
                errMsg = errMsg.substring(0, 2000);
            }
            logEntity.setErrorMsg(errMsg);
            throw e;
        } finally {
            logEntity.setCostTime(System.currentTimeMillis() - startTime);
            logEntity.setCreateTime(new Date());
            try {
                logService.saveAsync(logEntity);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }
        return result;
    }

    /**
     * 获取当前HTTP请求
     *
     * @return 请求对象
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }

    /**
     * 获取客户端IP地址
     *
     * @param request 请求
     * @return IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 从控制器类或请求路径中提取模块名称(中文)
     *
     * @param pjp     连接点
     * @param request 请求
     * @return 中文模块名称
     */
    private String extractModule(ProceedingJoinPoint pjp, HttpServletRequest request) {
        String className = pjp.getSignature().getDeclaringType().getSimpleName();
        // 去掉Controller后缀后查中文映射表, 找不到则用原名
        String key = className.replace("Controller", "");
        String module = MODULE_MAP.get(key);
        return module != null ? module : key;
    }

    /**
     * 根据方法名和模块名生成具体的操作描述(结合模块名使其更直观)
     * 例如: method=add + module=商品管理 → "新增商品"
     *
     * @param methodName 控制器方法名
     * @param module     中文模块名
     * @return 操作描述
     */
    private String extractOperation(String methodName, String module) {
        String action = null;
        if (methodName.startsWith("add") || methodName.startsWith("save") || methodName.startsWith("create")) {
            action = "新增";
        } else if (methodName.startsWith("update") || methodName.startsWith("edit") || methodName.startsWith("modify")) {
            action = "修改";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            action = "删除";
        } else if (methodName.startsWith("audit") || methodName.startsWith("review")) {
            action = "审核";
        } else if (methodName.startsWith("unAudit") || methodName.startsWith("cancelAudit")) {
            action = "反审核";
        } else if (methodName.startsWith("import")) {
            action = "导入";
        } else if (methodName.startsWith("export")) {
            action = "导出";
        } else if (methodName.startsWith("resetPassword")) {
            action = "重置密码";
        } else if (methodName.startsWith("reset")) {
            action = "重置";
        } else if (methodName.startsWith("changePassword")) {
            action = "修改密码";
        } else if (methodName.startsWith("change")) {
            action = "修改";
        } else if (methodName.startsWith("assign")) {
            action = "分配";
        } else if (methodName.startsWith("status")) {
            action = "修改状态";
        } else if (methodName.startsWith("clear")) {
            action = "清空";
        } else if (methodName.startsWith("login")) {
            action = "登录";
        } else if (methodName.startsWith("logout")) {
            action = "退出";
        } else if (methodName.startsWith("void")) {
            action = "作废";
        }

        // 有明确动词 + 模块 → 拼成 "新增商品" 这样的完整描述
        if (action != null && module != null) {
            return action + extractSubject(module);
        }
        return action != null ? action : methodName;
    }

    /**
     * 从模块名中提取操作对象(去掉"管理/配置"等后缀)
     * 例如: "商品管理" → "商品", "采购订单" → "采购订单", "库存预警配置" → "库存预警"
     *
     * @param module 中文模块名
     * @return 操作对象
     */
    private String extractSubject(String module) {
        if (module == null) return "";
        // 去掉末尾的"管理"、"配置"、"认证"等泛指后缀
        if (module.endsWith("管理")) {
            return module.substring(0, module.length() - 2);
        }
        if (module.endsWith("配置")) {
            return module.substring(0, module.length() - 2);
        }
        if (module.endsWith("认证")) {
            return module.substring(0, module.length() - 2);
        }
        return module;
    }

    /**
     * 序列化方法参数为JSON字符串
     * 控制长度, 避免过大文本写入数据库
     *
     * @param args    参数列表
     * @param request 请求
     * @return JSON字符串
     */
    private String serializeArgs(Object[] args, HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg == null) {
                    continue;
                }
                // 跳过HttpServletRequest/Response等非业务参数
                String typeName = arg.getClass().getName();
                if (typeName.startsWith("javax.") || typeName.startsWith("org.springframework.")) {
                    continue;
                }
                paramMap.put("arg" + i, arg);
            }
            if (paramMap.isEmpty()) {
                return null;
            }
            String json = objectMapper.writeValueAsString(paramMap);
            // 限制参数长度
            if (json.length() > 2000) {
                json = json.substring(0, 2000) + "...(truncated)";
            }
            return json;
        } catch (Exception e) {
            return "参数序列化失败";
        }
    }
}
