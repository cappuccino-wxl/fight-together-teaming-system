package com.example.match.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.match.common.ErrorCode;
import com.example.match.exception.BusinessException;
import com.example.match.model.domain.Team;
import com.example.match.model.domain.User;
import com.example.match.model.domain.UserTeam;
import com.example.match.model.dto.TeamQuery;
import com.example.match.model.enums.TeamStatusEnum;
import com.example.match.model.request.TeamJoinRequest;
import com.example.match.model.request.TeamQuitRequest;
import com.example.match.model.request.TeamUpdateRequest;
import com.example.match.service.TeamService;
import com.example.match.mapper.TeamMapper;
import com.example.match.service.UserService;
import com.example.match.service.UserTeamService;
import com.example.match.vo.TeamUserVO;
import com.example.match.vo.UserVO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
* @author asus
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-05-10 16:16:34
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;

    /**
     * @description: 创建队伍
     * @param team
     * @param loginUser
     * @return long
     * @date: 2023/5/12 13:34
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务，插入失败回滚
    public long addTeam(Team team, User loginUser) {
        // 1. 请求参数不为空
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 2. 1 <= 队伍人数 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if(maxNum < 1 || maxNum > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        // 3. 队伍标题 <= 20
        String name = team.getName();
        if(StringUtils.isBlank(name) || name.length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍长度不满足要求");
        }
        // 4. 描述 <= 512
        String description = team.getDescription();
        if(StringUtils.isNotBlank(description) && description.length() > 512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        // 5. status 是否公开（0,1,2）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
        if(enumByValue == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        // 6. 如果status是加密状态，则密码 <= 32
        String password = team.getPassword();
        if(TeamStatusEnum.SECRET.equals(enumByValue)){
            if(StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        // 7. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if(new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
        // 8. 用户最多创建 5 个队伍
        // 有 bug，客户同时创建 100 个队伍
        QueryWrapper<Team> wrapper = new QueryWrapper<>();
        long userId = loginUser.getId();
        wrapper.eq("userId", userId);
        long count = this.count(wrapper);
        if(count >= 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }
        // 插入队伍信息到队伍列表
        team.setId(null);
        team.setUserId(userId);
        boolean save = this.save(team);
        Long teamId = team.getId();
        if(!save || teamId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "插入队伍信息失败");
        }
        // 插入用户 =》队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(0L);
        userTeam.setJoinTime(new Date());
        save = userTeamService.save(userTeam);
        if(!save){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    /**
     * @description: 查询队伍
     * @param teamQuery
     * @param isAdmin
     * @return java.util.List<com.example.match.vo.TeamUserVO>
     * @date: 2023/5/12 13:34
     */
    // 圈复杂度太高，可以把if-else抽出来
    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> wrapper = new QueryWrapper<>();
        // 组合查询条件
        if(teamQuery != null){
            // 根据id查询
            Long id = teamQuery.getId();
            if(id != null && id > 0){
                wrapper.eq("id", id);
            }
            // 根据id列表查询
            List<Long> idList = teamQuery.getIdList();
            if(!CollectionUtils.isEmpty(idList)){
                wrapper.in("id", idList);
            }
            // 根据搜索文本查询
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                wrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            // 根据队伍名查询
            String name = teamQuery.getName();
            if(StringUtils.isNotBlank(name)){
                wrapper.like("name", name);
            }
            // 根据队伍描述查询
            String description = teamQuery.getDescription();
            if(StringUtils.isNotBlank(description)){
                wrapper.like("description", description);
            }
            // 根据最大人数查询
            Integer maxNum = teamQuery.getMaxNum();
            if(maxNum !=  null && maxNum > 0){
                wrapper.eq("maxNum", maxNum);
            }
            // 根据用户id查询
            Long userId = teamQuery.getUserId();
            if(userId != null && userId > 0){
                wrapper.eq("userId", userId);
            }
            // 根据队伍状态查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if(statusEnum == null){
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if(!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            wrapper.eq("status", statusEnum.getValue());
        }
        // 不展示已过期的队伍
        // expireTime is null or expireTime > now()
        wrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> list = this.list(wrapper);
        if(CollectionUtils.isEmpty(list)){
            return new ArrayList<>();
        }
        // 关联查询用户信息
        // 1. 自己写SQL语句
        // 查询队伍和创建人的信息
        // select * from team t left join user u on t.userId = u.id
        // 查询队伍和已加入队伍的成员信息
        // select * from team t left join user_team ut on t.id = ut.teamId left join user u on ut.userId = u.id
        // 关联查询创建人的用户信息
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : list) {
            Long userId = team.getUserId();
            if(userId == null){
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 脱敏用户信息
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    /**
     * @description: 更新队伍信息
     * @param teamUpdateRequest
     * @param loginUser
     * @return boolean
     * @date: 2023/5/12 13:39
     */
    @Override
    public boolean updateTeams(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据id更新
        Long id = teamUpdateRequest.getId();
        Team oldTeam = getTeamById(id);
        // 只有管理员或创建的队伍可以修改
        if(!oldTeam.getId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 获取房间状态
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if(statusEnum.equals(TeamStatusEnum.SECRET)){
            if(StringUtils.isBlank(teamUpdateRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须有密码");
            }
        }
        Team team = new Team();
        // 将 teamUpdateRequest 赋值给 team（部分属性）
        BeanUtils.copyProperties(teamUpdateRequest, team);
        return this.updateById(team);
    }

    /**
     * @description: 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return boolean
     * @date: 2023/5/12 13:54
     */
    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if(teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        if(team.getExpireTime() != null && team.getExpireTime().before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        Integer teamStatus = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(teamStatus);
        if(TeamStatusEnum.PRIVATE.equals(teamStatusEnum)){ // 最好把确定不是空的放在equals前面
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if(StringUtils.isBlank(password) || !password.equals(team.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        // 该用户已加入的队伍数量
        Long userId = loginUser.getId();
        // 只有一个线程能获取到锁
        RLock lock = redissonClient.getLock("yupao:join_team");
        try {
            // 抢到锁并执行
            while (true) {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    System.out.println("getLock: " + Thread.currentThread().getId());
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if (hasJoinNum > 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入 5 个队伍");
                    }
                    // 不能重复加入已加入的队伍
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    userTeamQueryWrapper.eq("teamId", teamId);
                    long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if (hasUserJoinTeam > 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
                    }
                    // 已加入队伍的人数
                    long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
                    if (teamHasJoinNum >= team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }
                    // 修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
            return false;
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        // 队伍只剩一人，解散
        if (teamHasJoinNum == 1) {
            // 删除队伍
            this.removeById(teamId);
        } else {
            // 队伍还剩至少两人
            // 是队长
            if (team.getUserId() == userId) {
                // 把队伍转移给最早加入的用户
                // 1. 查询已加入队伍的所有用户和加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                // 更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
        }
        // 移除关系
        return userTeamService.remove(queryWrapper);
    }

    // 解散队伍（删除）
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        Team team = this.getTeamById(id);
        Long teamId = team.getId();
        // 如果当前登录用户表示队长
        if(team.getUserId() != loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH, "无访问权限");
        }
        // 移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("teamId", teamId);
        boolean remove = userTeamService.remove(wrapper);
        if(!remove){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息异常");
        }
        return this.removeById(teamId);
    }

    private Team getTeamById(Long teamId) {
        if(teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    private long countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }

}




