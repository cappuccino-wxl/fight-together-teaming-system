package com.example.match.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.match.model.domain.UserTeam;
import com.example.match.service.UserTeamService;
import com.example.match.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author asus
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-05-10 16:19:26
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




