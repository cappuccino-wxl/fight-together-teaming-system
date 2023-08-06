import myAxios from "../plugins/myAxios";
import { setCurrentUserState } from "../states/user";

// 从后端获取当前登录的用户信息
export const getCurrentUser = async () => {
    // 本地缓存
    // const currentUser = getCurrentUserState();
    // if (currentUser) {
    //     return currentUser;
    // }
    // 不存在则从远程获取
    const res = await myAxios.get('/user/current');
    if (res.data.code === 0) {
        setCurrentUserState(res.data);
        return res.data;
    }
    return null;
}

