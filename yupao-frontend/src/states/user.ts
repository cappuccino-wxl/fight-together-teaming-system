import {UserType} from "../models/user";

// 定义当前用户的set和get函数
let currentUser: UserType;

// 设置当前用户状态
const setCurrentUserState = (user: UserType) => {
    currentUser = user;
}
// 获取当前用户状态
const getCurrentUserState = () : UserType => {
    return currentUser;
}

export {
    setCurrentUserState,
    getCurrentUserState,
}
