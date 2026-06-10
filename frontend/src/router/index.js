import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  // 1. 默认重定向到登录页
  { 
    path: '/', 
    redirect: '/login' 
  },
  // 2. 登录/注册/重置密码页面
  { 
    path: '/login', 
    name: 'Login',
    component: () => import('@/views/login/index.vue') 
  },
  // 3. 管理端布局容器
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/layout/AdminLayout.vue'),
    redirect: '/admin/subject', // 进入管理端默认显示科目管理
    children: [
      { 
        path: 'subject', 
        name: 'SubjectManage',
        component: () => import('@/views/admin/subject.vue'),
        meta: { title: '科目管理' }
      },
      { 
        path: 'knowledge', 
        name: 'KnowledgeManage',
        component: () => import('@/views/admin/knowledgepoint.vue'),
        meta: { title: '知识点管理' }
      },
      { 
        path: 'resource', 
        name: 'ResourceManage',
        component: () => import('@/views/admin/resource.vue'),
        meta: { title: '资源管理' }
      },
      { 
        path: 'ai-match', 
        name: 'AiMatch',
        component: () => import('@/views/admin/aiMatch.vue'),
        meta: { title: 'AI资源重组匹配' }
      },
      {
        path: 'assessment-question', 
        name: 'AssessmentQuestion',
        component: () => import('@/views/admin/AssessmentQuestion.vue'),
        meta: { title: '画像测试题管理' }
      }
    ]
  },
  // 4. 用户端布局容器
  {
    path: '/user',
    name: 'User',
    component: () => import('@/views/layout/UserLayout.vue'),
    redirect: '/user/profile', // 进入用户端默认显示用户画像
    children: [
      { 
        path: 'profile', 
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '用户画像' }
      },
      { 
        path: 'aiteacher', 
        name: 'AiTeacher',
        component: () => import('@/views/user/AiTeacher.vue'),
        meta: { title: 'AI教师' }
      },
      { 
        path: 'learningpath', 
        name: 'LearningPath',
        component: () => import('@/views/user/LearningPath.vue'),
        meta: { title: '学习路径' }
      },
      { 
        path: 'companion', 
        name: 'Companion',
        component: () => import('@/views/user/Companion.vue'),
        meta: { title: 'AI陪伴' }
      },
      { 
        path: 'analytics', 
        name: 'Analytics',
        component: () => import('@/views/user/Analytics.vue'),
        meta: { title: '学习分析' }
      },
      { 
        path: 'information', 
        name: 'Information',
        component: () => import('@/views/user/Information.vue'),
        meta: { title: '个人信息' }
      },

      
    ]
  },
  // 5. 404 页面配置（可选，防止路径输入错误显示空白）
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫：如果没有 Token 且不在登录页，强制跳转登录
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  const userInfoStr = localStorage.getItem('userInfo');
  let userRole = null;
  
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr);
      userRole = userInfo.role;
    } catch (e) {
      console.error('解析用户信息失败', e);
    }
  }
  
  // 如果访问的是登录页且已有token，则根据角色跳转
  if (to.path === '/login' && token) {
    if (userRole === 'ADMIN') {
      next('/admin/subject');
    } else {
      next('/user/profile');
    }
    return;
  }
  
  // 如果访问管理端但没有token或非管理员角色
  if (to.path.startsWith('/admin')) {
    if (!token) {
      next('/login');
      return;
    }
    if (userRole !== 'ADMIN') {
      next('/login'); // 或者可以跳转到错误页面
      return;
    }
  }
  
  // 如果访问用户端但没有token或非用户角色
  if (to.path.startsWith('/user')) {
    if (!token) {
      next('/login');
      return;
    }
    if (userRole !== 'USER' && userRole !== 'ADMIN') {
      next('/login'); // 或者可以跳转到错误页面
      return;
    }
  }
  
  next();
});

export default router;