import { createRouter, createWebHistory, type RouteLocationNormalized } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import AdminView from '../views/AdminView.vue'
import AuthView from '../views/AuthView.vue'
import CoursesView from '../views/CoursesView.vue'
import ArticlesView from '../views/ArticlesView.vue'
import { api } from '../lib/api'
import { authToken } from '../lib/auth'

async function courseAccessGuard(to: RouteLocationNormalized) {
  const courseId = String(to.params.courseId)
  const isContentRoute = to.name === 'course-content'
  const courseDetail = { name: 'course-detail', params: { courseId } }
  const courseContent = { name: 'course-content', params: { courseId } }

  if (!authToken.value) return isContentRoute ? courseDetail : true

  try {
    const isUserEnrolled = await api<boolean>(`/enrollments/courses/${courseId}/me`)
    if (isUserEnrolled) return isContentRoute ? true : courseContent
    return isContentRoute ? courseDetail : true
  } catch {
    // An expired token should not prevent the public course-detail page from loading.
    return true
  }
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/admin',
      name: 'admin',
      component: AdminView,
    },
    {
      path: '/auth',
      name: 'auth',
      component: AuthView,
    },
    {
      path: '/courses',
      name: 'courses',
      component: CoursesView,
    },
    {
      path: '/courses/:courseId',
      name: 'course-detail',
      component: () => import('../views/CourseDetailView.vue'),
      beforeEnter: courseAccessGuard,
    },
    {
      path: '/courses/:courseId/content',
      name: 'course-content',
      component: () => import('../views/CourseContentView.vue'),
      beforeEnter: courseAccessGuard,
    },
    {
      path: '/articles',
      name: 'articles',
      component: ArticlesView,
    },
    { path: '/articles/:articleId', name: 'article-detail', component: () => import('../views/ArticleDetailView.vue') },
  ],
})

export default router
