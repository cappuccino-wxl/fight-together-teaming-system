package com.example.match.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.match.common.BaseResponse;
import com.example.match.common.DeleteRequest;
import com.example.match.common.ErrorCode;
import com.example.match.common.ResultUtils;
import com.example.match.exception.BusinessException;
import com.example.match.model.domain.Team;
import com.example.match.model.domain.User;
import com.example.match.model.domain.UserTeam;
import com.example.match.model.dto.TeamQuery;
import com.example.match.model.request.TeamAddRequest;
import com.example.match.model.request.TeamJoinRequest;
import com.example.match.model.request.TeamQuitRequest;
import com.example.match.model.request.TeamUpdateRequest;
import com.example.match.service.TeamService;
import com.example.match.service.UserService;
import com.example.match.service.UserTeamService;
import com.example.match.vo.TeamUserVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 队伍的相关功能
 */
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:5173"})
@Slf4j
public class TeamController {
    @Resource
    private UserService userService;
    @Resource
    private TeamService teamService;
    @Resource
    private UserTeamService userTeamService;

    /**
     * @description: 添加队伍
     * @param teamAddRequest
     * @param request
     * @return com.example.match.common.BaseResponse<java.lang.Long> 队伍id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if(teamAddRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    /**
     * @description: 更新队伍
     * @param teamUpdateRequest
     * @param request
     * @return com.example.match.common.BaseResponse<java.lang.Boolean>
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request){
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean res = teamService.updateTeams(teamUpdateRequest, loginUser);
        if(!res){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍更新失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * @description: 根据id查询队伍信息
     * @param id
     * @return com.example.match.common.BaseResponse<com.example.match.model.domain.Team>
     */
    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(Long id){
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

    /**
     * @description: 根据条件查询队伍信息
     * @param teamQuery
     * @param request
     * @return com.example.match.common.BaseResponse<java.util.List<com.example.match.vo.TeamUserVO>>
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        // 判断当前用户是否已加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {}
        // 查询已加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        return ResultUtils.success(teamList);
    }

    /**
     * @description: 分页查询
     * @param teamQuery
     * @return com.example.match.common.BaseResponse<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.example.match.model.domain.Team>>
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        // 选择性地把 teamQuery 里的属性复制到 team 里
        BeanUtils.copyProperties(teamQuery, team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(),teamQuery.getPageSize());
        QueryWrapper<Team> wrapper = new QueryWrapper<>(team);
        Page<Team> res = teamService.page(page, wrapper);
        return ResultUtils.success(res);
    }

    /**
     * @description: 加入队伍
     * @param teamJoinRequest
     * @param request
     * @return com.example.match.common.BaseResponse<java.lang.Boolean>
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request){
        // TODO 优化点，老是判空很麻烦，可以写个AOP
        if(teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean res = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(res);
    }

    /**
     * @description: 退出队伍
     * @param teamQuitRequest
     * @param request
     * @return com.example.match.common.BaseResponse<java.lang.Boolean>
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean res = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(res);
    }

    /**
     * @description: 解散队伍
     * @param deleteRequest
     * @param request
     * @return com.example.match.common.BaseResponse<java.lang.Boolean>
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        if(deleteRequest == null || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean res = teamService.deleteTeam(deleteRequest.getId(), loginUser);
        if(!res){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍删除失败");
        }
        return ResultUtils.success(true);
    }


    /**
     * @description: 查询我创建的队伍
     * @param teamQuery
     * @param request
     * @return com.example.match.common.BaseResponse<java.util.List<com.example.match.vo.TeamUserVO>>
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request); // TODO 每次都要判断是否是当前登录用户，可以写个拦截器
        // 复用listTeams方法，只新增查询条件，不做修改，开闭原则
        List<TeamUserVO> list = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(list);
    }

    /**
     * @description: 查询我加入的队伍
     * @param teamQuery
     * @param request
     * @return com.example.match.common.BaseResponse<java.util.List<com.example.match.vo.TeamUserVO>>
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(wrapper);
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVO> list = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(list);
    }

}
