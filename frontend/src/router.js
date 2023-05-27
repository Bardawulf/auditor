import Vue from 'vue'
import Router from 'vue-router'
import store from '@/store' // Import your Vuex store

Vue.use(Router)

export const router = new Router({
  mode: 'history',
  base: process.env.BASE_URL,
  routes: [
    {
      path: '/',
      component: () => import('@/views/Index'),
      children: [
        {
          name: 'Curriculums',
          path: '/',
          component: () => import('@/views/Curriculum/List'),
          meta: { requiresAuth: true } // Add requiresAuth meta field
        },
        {
          name: 'Curriculum',
          path: '/curriculum/:id',
          component: () => import('@/views/Curriculum/Index'),
        },
        {
          name: 'Curriculum-edit',
          path: '/curriculum/edit/:id',
          props: route => ({ action: route.query.action }),
          component: () => import('@/views/Curriculum/Edit'),
        },
        {
          name: 'Curriculum-create',
          path: '/curriculum/create',
          props: route => ({ action: route.query.action }),
          component: () => import('@/views/Curriculum/Edit'),
        },
        {
          name: 'Students',
          path: '/students-list',
          component: () => import('@/views/Student/List'),
        },
        {
          name: 'Student Audit',
          path: '/student/:id',
          component: () => import('@/views/Student/Audit'),
        },
        {
          name: 'Compare Students',
          path: '/students-comparison/',
          props: route => ({ id: route.query.id }),
          component: () => import('@/views/Student/Comparison'),
        },
        {
          name: 'Compare Students Performance',
          path: '/students-performance-comparison/',
          props: route => ({ ids: route.query.ids, curriculum: route.query.curriculum }),
          component: () => import('@/views/Student/PerformanceComparison'),
        },
        {
          name: 'Template mails',
          path: '/mails-list',
          component: () => import('@/views/Mail/List'),
        },
        {
          name: 'Mail',
          path: '/modify-mail',
          props: route => ({ action: route.query.action, id: route.query.id }),
          component: () => import('@/views/Mail/Modify')
        },
        {
          name: 'Manuals list',
          path: 'manuals-list',
          component: () => import('@/views/manuals/List')
        },
        {
          name: 'Curriculum manual',
          path: 'manual-curriculum',
          component: () => import('@/views/manuals/Curriculum')
        },
        {
          name: 'Student manual',
          path: 'manual-student',
          component: () => import('@/views/manuals/Student')
        },
        {
          name: 'Login',
          path: '/login',
          component: () => import('@/views/Login/Login'),
        },
      ],
    },
  ],
})




// Navigation guard
router.beforeEach((to, from, next) => {
  const isAuthenticated = store.getters.isAuthenticated; // Assuming you have a getter in your Vuex store to check if the user is authenticated

  if (to.name !== 'Login' && !isAuthenticated) {
    next({ name: 'Login' }); // Redirect to the "Login" route if not authenticated
  } else {
    next(); // Proceed with the navigation
  }
})

export default router;