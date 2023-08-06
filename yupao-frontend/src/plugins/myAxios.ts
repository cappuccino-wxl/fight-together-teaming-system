import axios, {AxiosInstance} from "axios";

// const isDev = process.env.NODE_ENV === 'development';

const myAxios: AxiosInstance = axios.create({
    baseURL:'http://localhost:8082/api',
});

myAxios.defaults.withCredentials = true; // 配置为true

// 请求拦截器
myAxios.interceptors.request.use(function (config) {
    console.log('我要发请求啦', config)
    // Do something before request is sent
    return config;
}, function (error) {
    // Do something with request error
    return Promise.reject(error);
});

// 响应拦截器
myAxios.interceptors.response.use(function (response) {
    console.log('我收到你的响应啦', response)
    // 未登录则跳转到登录页
    if (response?.data?.code === 40100) {
        const redirectUrl = window.location.href;
        console.log("redirectUrl", redirectUrl)
        window.location.href = `/user/login?redirect=${redirectUrl}`;
    }
    // Do something with response data
    return response.data;
}, function (error) {
    // Do something with response error
    return Promise.reject(error);
});

export default myAxios;
