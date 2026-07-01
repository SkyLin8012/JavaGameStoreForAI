package com.steam.service.impl;

import com.steam.dao.MemberDao;
import com.steam.dao.impl.MemberDaoImpl;
import com.steam.exception.SteamException;
import com.steam.model.Member;
import com.steam.service.MemberService;

import java.sql.SQLException;
import java.util.List;

public class MemberServiceImpl implements MemberService {
    private MemberDao memberDao = new MemberDaoImpl();

    @Override
    public Member login(String username, String password) throws SteamException {
        try {
            Member m = memberDao.findByUsername(username);
            if (m != null && m.getPassword().equals(password)) {
                return m;
            }
            throw new SteamException("使用者名稱或密碼錯誤！");
        } catch (SQLException e) {
            throw new SteamException("登入資料庫查詢異常", e);
        }
    }

    @Override
    public boolean register(Member member) throws SteamException {
        try {
            if (memberDao.findByUsername(member.getUsername()) != null) {
                throw new SteamException("使用者名稱已被註冊！");
            }
            return memberDao.insert(member);
        } catch (SQLException e) {
            throw new SteamException("註冊寫入異常", e);
        }
    }

    @Override
    public boolean updateProfile(Member member) throws SteamException {
        try {
            return memberDao.update(member);
        } catch (SQLException e) {
            throw new SteamException("更新會員資料失敗", e);
        }
    }

    @Override
    public boolean deleteMember(int id) throws SteamException {
        try {
            return memberDao.delete(id);
        } catch (SQLException e) {
            throw new SteamException("刪除會員失敗", e);
        }
    }

    @Override
    public List<Member> getAllMembers() throws SteamException {
        try {
            return memberDao.findAll();
        } catch (SQLException e) {
            throw new SteamException("取得所有會員資料失敗", e);
        }
    }

    @Override
    public Member getMemberById(int id) throws SteamException {
        try {
            return memberDao.findById(id);
        } catch (SQLException e) {
            throw new SteamException("查詢會員失敗", e);
        }
    }
}
