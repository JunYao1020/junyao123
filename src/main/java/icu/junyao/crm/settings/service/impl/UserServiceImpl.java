package icu.junyao.crm.settings.service.impl;

import icu.junyao.crm.exception.LoginException;
import icu.junyao.crm.exception.RegisterException;
import icu.junyao.crm.settings.dao.UserDao;
import icu.junyao.crm.settings.domain.User;
import icu.junyao.crm.settings.service.UserService;
import icu.junyao.crm.utils.DateTimeUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author wu
 */
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserDao userDao;

    @Override
    public User login(String loginAct, String loginPwd, String ip) throws LoginException {
        User user = userDao.login(loginAct, loginPwd);
        String lock = "0";
        if (user == null) {
            throw new LoginException("账号密码错误!");
        }
        String expireTime = user.getExpireTime();
        String currentTime = DateTimeUtil.getSysTime();
        if (expireTime.compareTo(currentTime) < 0) {
            throw new LoginException("账号已失效");
        }
        String lockState = user.getLockState();
        if (lock.equals(lockState)) {
            throw new LoginException("账号已锁定");
        }
        String allowIps = user.getAllowIps();
        String releaseIp = "vanessa";
        if (allowIps != null && !"".equals(allowIps) && !allowIps.contains(releaseIp) && !allowIps.contains(ip)) {
            throw new LoginException("您的ip地址被限制登录");
        }
        return user;
    }

    @Override
    public void register(User user) throws RegisterException {
        if (userDao.selectUserByAct(user.getLoginAct()) == 1) {
            throw new RegisterException("该邮箱已被注册, 请换一个邮箱重新注册");
        }
        int num = userDao.register(user);
        if (num == 0) {
            throw new RegisterException("账号注册失败!");
        }
    }

    @Override
    public String getPwdById(String id) {
        return userDao.getPwdById(id);
    }

    @Override
    public int updatePwd(String newPwd, String id) {
        return userDao.updatePwd(newPwd, id);
    }
}
