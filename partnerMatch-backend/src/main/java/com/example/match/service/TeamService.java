package com.example.match.service;

import com.example.match.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.match.model.domain.User;
import com.example.match.model.dto.TeamQuery;
import com.example.match.model.request.TeamJoinRequest;
import com.example.match.model.request.TeamQuitRequest;
import com.example.match.model.request.TeamUpdateRequest;
import com.example.match.vo.TeamUserVO;

import java.util.List;

/**
* @author asus
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-05-10 16:16:34
*/
public interface TeamService extends IService<Team> {
    /**
     * @description: 创建队伍
     * @param team
     * @param loginUser
     * @return long
     * @date: 2023/5/10 17:59
     */
    long addTeam(Team team, User loginUser);

    /**
     * @param teamQuery
     * @param isAdmin
     * @return java.util.List<com.example.match.vo.TeamUserVO>
     * @description: 搜索队伍
     * @date: 2023/5/11 8:27
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * @description: 更新队伍信息
     * @param teamUpdateRequest
     * @param loginUser
     * @return boolean
     * @date: 2023/5/11 10:24
     */
    boolean updateTeams(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * @description: 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return boolean
     * @date: 2023/5/11 10:54
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * @param teamQuitRequest
     * @param loginUser
     * @return boolean
     * @description: 退出队伍
     * @date: 2023/5/11 14:25
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * @description: 解散队伍（删除队伍）
     * @param id
     * @param loginUser
     * @return boolean
     * @date: 2023/5/11 16:00
     */
    boolean deleteTeam(long id, User loginUser);
}
