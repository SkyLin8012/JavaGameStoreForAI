package com.steam.service;

import com.steam.exception.SteamException;
import com.steam.model.Member;
import java.util.List;

public interface MemberService {
    Member login(String username, String password) throws SteamException;
    boolean register(Member member) throws SteamException;
    boolean updateProfile(Member member) throws SteamException;
    boolean deleteMember(int id) throws SteamException;
    List<Member> getAllMembers() throws SteamException;
    Member getMemberById(int id) throws SteamException;
}
