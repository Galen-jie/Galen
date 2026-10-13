export const routes = [
  {
    path: '/',
    component: () => import('@/components/layout/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'pet',
        name: 'PetList',
        component: () => import('@/views/pet/PetList.vue'),
        meta: { title: '宠物列表' }
      },
      {
        path: 'pet/:id',
        name: 'PetDetail',
        component: () => import('@/views/pet/PetDetail.vue'),
        meta: { title: '宠物详情' }
      },
      {
        path: 'pet/publish',
        name: 'PetPublish',
        component: () => import('@/views/pet/PetPublish.vue'),
        meta: { title: '发布宠物', requireAuth: true }
      },
      {
        path: 'pet/my',
        name: 'MyPets',
        component: () => import('@/views/pet/MyPets.vue'),
        meta: { title: '我的发布', requireAuth: true }
      },
      {
        path: 'adoption/my',
        name: 'MyApplications',
        component: () => import('@/views/adoption/MyApplications.vue'),
        meta: { title: '我的申请', requireAuth: true }
      },
      {
        path: 'adoption/pending',
        name: 'PendingReviews',
        component: () => import('@/views/adoption/PendingReviews.vue'),
        meta: { title: '待审核申请', requireAuth: true, requireRole: 1 }
      },
      {
        path: 'goods',
        name: 'GoodsList',
        component: () => import('@/views/goods/GoodsList.vue'),
        meta: { title: '商品列表' }
      },
      {
        path: 'goods/:id',
        name: 'GoodsDetail',
        component: () => import('@/views/goods/GoodsDetail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/goods/Cart.vue'),
        meta: { title: '购物车', requireAuth: true }
      },
      {
        path: 'seckill',
        name: 'SeckillList',
        component: () => import('@/views/seckill/SeckillList.vue'),
        meta: { title: '秒杀专区' }
      },
      {
        path: 'seckill/:id',
        name: 'SeckillDetail',
        component: () => import('@/views/seckill/SeckillDetail.vue'),
        meta: { title: '秒杀详情' }
      },
      {
        path: 'seckill/orders',
        name: 'MySeckillOrders',
        component: () => import('@/views/seckill/MySeckillOrders.vue'),
        meta: { title: '我的秒杀订单', requireAuth: true }
      },
      {
        path: 'user/profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人中心', requireAuth: true }
      },
      {
        path: 'user/settings',
        name: 'Settings',
        component: () => import('@/views/user/Settings.vue'),
        meta: { title: '个人设置', requireAuth: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面未找到' }
  }
]