// 用户信息类型
export type UserType = {
    url: string | undefined;
    id: number;
    username: string;
    userAccount: string;
    avatarUrl: string;
    profile: string,
    gender: number;
    phone: string;
    email: string;
    userStatus: number;
    userRole: number;
    planetCode: string;
    createTime: Date;
    tags: string;
  };