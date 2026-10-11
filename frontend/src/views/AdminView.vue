<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { authToken, isAdmin, setAuthToken, signOut, verifyAdminAccess } from '@/lib/auth'
import { api } from '@/lib/api'

type Tab =
  | 'dashboard'
  | 'profile'
  | 'article'
  | 'course'
  | 'lesson'
  | 'section'
  | 'project'
  | 'certificate'
  | 'experience'
  | 'student'
  | 'linkedin'
type Enrollment = { id: string; studentId: string; studentName: string; courseId: string; courseTitle: string; paymentStatus: string; enrolledAt: string }
type Course = {
  id: string
  title: string
  description: string
  price: number
  thumbnailLink: string | null
  status: 'ACTIVE' | 'COMPLETED' | 'ARCHIVED'
  enrollments?: Enrollment[]
}
type Article = { id: string; title: string; content: string; tags: string[] }
type Lesson = { id: string; courseId: string; title: string; videoLink: string | null }
type CourseSection = {
  id: string
  courseId: string
  title: string
  description: string | null
  orderIndex: number
}
type Project = {
  id: string
  title: string
  description: string | null
  imagePath: string | null
  projectUrl: string
  displayOrder: number
}
type Certificate = {
  id: string
  title: string
  issuer: string | null
  issuedDate: string | null
  imagePath: string
  displayOrder: number
}
type WorkExperience = {
  id: string
  company: string
  role: string
  startDate: string
  endDate: string | null
  summary: string
  highlights: string[]
}
type Student = { id: string; name: string; email: string }
type AdminProfile = {
  name: string
  headline: string | null
  biography: string | null
  photoUrl: string | null
}
type LinkedInConnection = {
  configured: boolean
  connected: boolean
  personUrn: string | null
  expiresAt: string | null
}
const activeTab = ref<Tab>('dashboard'),
  message = ref(''),
  error = ref(''),
  loading = ref(false),
  email = ref(''),
  password = ref('')
const profile = ref({ name: '', headline: '', biography: '', photoUrl: '' })
const article = ref({ title: '', content: '', tags: '' })
const course = ref({ title: '', description: '', price: '0', thumbnailLink: '', status: 'ACTIVE' as Course['status'] })
const lesson = ref({ courseId: '', title: '', videoLink: '' })
const section = ref({ courseId: '', title: '', description: '', orderIndex: '0' })
const project = ref({
  title: '',
  description: '',
  imagePath: '',
  projectUrl: '',
  displayOrder: '0',
})
const certificate = ref({ title: '', issuer: '', issuedDate: '', imagePath: '', displayOrder: '0' })
const experience = ref({
  company: '',
  role: '',
  startDate: '',
  endDate: '',
  summary: '',
  highlights: '',
  displayOrder: '0',
})
const student = ref({ name: '', email: '', password: '' })
const courses = ref<Course[]>([])
const articles = ref<Article[]>([]),
  lessons = ref<Lesson[]>([]),
  sections = ref<CourseSection[]>([])
const projects = ref<Project[]>([]),
  certificates = ref<Certificate[]>([]),
  experiences = ref<WorkExperience[]>([]),
  students = ref<Student[]>([])
const editingArticleId = ref<string | null>(null),
  editingCourseId = ref<string | null>(null),
  editingLessonId = ref<string | null>(null),
  editingSectionId = ref<string | null>(null),
  editingProjectId = ref<string | null>(null),
  editingCertificateId = ref<string | null>(null),
  editingExperienceId = ref<string | null>(null),
  editingStudentId = ref<string | null>(null)
const linkedIn = ref<LinkedInConnection | null>(null)
const studentQuery = ref('')
const courseQuery = ref('')
const articleQuery = ref('')
const selectedCourseId = ref<string | null>(null)
const selectedStudentId = ref<string | null>(null)
const visibleStudents = computed(() => {
  const query = studentQuery.value.trim().toLowerCase()
  return query
    ? students.value.filter((item) => `${item.name} ${item.email}`.toLowerCase().includes(query))
    : students.value
})
const selectedStudent = computed(() => students.value.find((item) => item.id === selectedStudentId.value) ?? null)
const selectedStudentEnrollments = computed(() =>
  courses.value.flatMap((course) => course.enrollments ?? []).filter((item) => item.studentId === selectedStudentId.value),
)
function studentEnrollmentCount(studentId: string) {
  return courses.value.reduce((total, course) => total + (course.enrollments ?? []).filter((item) => item.studentId === studentId).length, 0)
}
const activeCourses = computed(() => courses.value.filter((item) => item.status === 'ACTIVE'))
const completedCourses = computed(() => courses.value.filter((item) => item.status === 'COMPLETED'))
const visibleCourses = computed(() => {
  const query = courseQuery.value.trim().toLowerCase()
  return query
    ? courses.value.filter((item) => `${item.title} ${item.description}`.toLowerCase().includes(query))
    : courses.value
})
const visibleArticles = computed(() => {
  const query = articleQuery.value.trim().toLowerCase()
  return query
    ? articles.value.filter((item) => `${item.title} ${item.tags.join(' ')}`.toLowerCase().includes(query))
    : articles.value
})
const selectedCourse = computed(() => courses.value.find((item) => item.id === selectedCourseId.value) ?? null)
const tabs: { id: Tab; label: string; icon: string }[] = [
  { id: 'dashboard', label: 'Overview', icon: '⌂' },
  { id: 'course', label: 'Courses', icon: '▣' },
  { id: 'student', label: 'Students', icon: '◉' },
  { id: 'article', label: 'Articles', icon: '✦' },
  { id: 'profile', label: 'Ryan profile', icon: '☺' },
  { id: 'project', label: 'Projects', icon: '↗' },
  { id: 'certificate', label: 'Certificates', icon: '◇' },
  { id: 'experience', label: 'Experience', icon: '▤' },
  { id: 'linkedin', label: 'LinkedIn', icon: 'in' },
]
const activeTitle = computed(() => tabs.find((tab) => tab.id === activeTab.value)?.label ?? 'Admin')
function feedback() {
  message.value = ''
  error.value = ''
}
async function submit(action: () => Promise<void>, success: string) {
  feedback()
  loading.value = true
  try {
    await action()
    message.value = success
  } catch (reason) {
    error.value =
      reason instanceof Error ? reason.message : 'Something went wrong. Please try again.'
  } finally {
    loading.value = false
  }
}
async function loadProfile() {
  const saved = await api<AdminProfile>('/admin/profile')
  profile.value = {
    name: saved.name,
    headline: saved.headline ?? '',
    biography: saved.biography ?? '',
    photoUrl: saved.photoUrl ?? '',
  }
}
async function loadCourses() {
  courses.value = await api<Course[]>('/courses')
}
async function loadArticles() {
  articles.value = await api<Article[]>('/articles')
}
async function loadLessons(courseId = lesson.value.courseId) {
  lessons.value = courseId ? await api<Lesson[]>(`/lessons/course/${courseId}`) : []
}
async function loadSections(courseId = section.value.courseId) {
  sections.value = courseId ? await api<CourseSection[]>(`/sections/course/${courseId}`) : []
}
async function loadPortfolio() {
  const [projectItems, certificateItems, experienceItems] = await Promise.all([
    api<Project[]>('/projects'),
    api<Certificate[]>('/certificates'),
    api<WorkExperience[]>('/work-experiences'),
  ])
  projects.value = projectItems
  certificates.value = certificateItems
  experiences.value = experienceItems
}
async function loadStudents() {
  students.value = await api<Student[]>('/students')
}
async function loadLinkedIn() {
  linkedIn.value = await api<LinkedInConnection>('/linkedin/connection')
}
async function login() {
  await submit(async () => {
    const auth = await api<{ token: string; role: string }>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email: email.value, password: password.value }),
    })
    setAuthToken(auth.token, auth.role)
    if (!(await verifyAdminAccess())) throw new Error('This account is not an administrator.')
    await Promise.all([
      loadProfile(),
      loadCourses(),
      loadArticles(),
      loadPortfolio(),
      loadStudents(),
      loadLinkedIn(),
    ])
  }, 'Signed in.')
}
async function saveProfile() {
  await submit(async () => {
    await api('/admin/profile', {
      method: 'PUT',
      body: JSON.stringify({
        ...profile.value,
        headline: profile.value.headline || null,
        biography: profile.value.biography || null,
        photoUrl: profile.value.photoUrl || null,
      }),
    })
  }, 'Profile saved.')
}
async function uploadProfilePhoto(event: Event) {
  const photo = (event.target as HTMLInputElement).files?.[0]
  if (!photo) return
  await submit(async () => {
    const form = new FormData()
    form.append('photo', photo)
    const saved = await api<AdminProfile>('/admin/profile/photo', { method: 'POST', body: form })
    profile.value.photoUrl = saved.photoUrl ?? ''
  }, 'Profile photo uploaded.')
}
function resetArticle() {
  article.value = { title: '', content: '', tags: '' }
  editingArticleId.value = null
}
function resetCourse() {
  course.value = { title: '', description: '', price: '0', thumbnailLink: '', status: 'ACTIVE' }
  editingCourseId.value = null
}
async function openCourse(item: Course) {
  selectedCourseId.value = item.id
  editCourse(item)
  resetLesson(item.id)
  resetSection(item.id)
  await Promise.all([loadLessons(item.id), loadSections(item.id)])
}
function resetLesson(courseId = lesson.value.courseId) {
  lesson.value = { courseId, title: '', videoLink: '' }
  editingLessonId.value = null
}
async function saveArticle() {
  await submit(
    async () => {
      const body = {
        title: article.value.title,
        content: article.value.content,
        tags: article.value.tags
          .split(',')
          .map((tag) => tag.trim())
          .filter(Boolean),
      }
      await api(
        editingArticleId.value ? `/admin/articles/${editingArticleId.value}` : '/admin/articles',
        { method: editingArticleId.value ? 'PUT' : 'POST', body: JSON.stringify(body) },
      )
      resetArticle()
      await loadArticles()
    },
    editingArticleId.value ? 'Article updated.' : 'Article published.',
  )
}
function editArticle(item: Article) {
  article.value = { title: item.title, content: item.content, tags: item.tags.join(', ') }
  editingArticleId.value = item.id
}
async function deleteArticle(id: string) {
  if (!window.confirm('Delete this article?')) return
  await submit(async () => {
    await api(`/admin/articles/${id}`, { method: 'DELETE' })
    if (editingArticleId.value === id) resetArticle()
    await loadArticles()
  }, 'Article deleted.')
}
async function saveCourse() {
  await submit(
    async () => {
      const body = {
        ...course.value,
        price: Number(course.value.price),
        thumbnailLink: course.value.thumbnailLink || null,
      }
      const saved = await api<Course>(
        editingCourseId.value ? `/courses/${editingCourseId.value}` : '/courses',
        { method: editingCourseId.value ? 'PUT' : 'POST', body: JSON.stringify(body) },
      )
      resetCourse()
      selectedCourseId.value = saved.id
      lesson.value.courseId = saved.id
      section.value.courseId = saved.id
      await loadCourses()
    },
    editingCourseId.value
      ? 'Course updated.'
      : 'Course created. You can add lessons and sections below.',
  )
}
function editCourse(item: Course) {
  course.value = {
    title: item.title,
    description: item.description,
    price: String(item.price),
    thumbnailLink: item.thumbnailLink ?? '',
    status: item.status,
  }
  editingCourseId.value = item.id
}
async function deleteCourse(id: string) {
  if (!window.confirm('Delete this course and its content?')) return
  await submit(async () => {
    await api(`/courses/${id}`, { method: 'DELETE' })
    if (editingCourseId.value === id) resetCourse()
    if (lesson.value.courseId === id) resetLesson('')
    if (section.value.courseId === id) {
      section.value.courseId = ''
      sections.value = []
    }
    if (selectedCourseId.value === id) selectedCourseId.value = null
    await loadCourses()
  }, 'Course deleted.')
}
async function saveLesson() {
  await submit(
    async () => {
      const body = editingLessonId.value
        ? { title: lesson.value.title, videoLink: lesson.value.videoLink || null }
        : { ...lesson.value, videoLink: lesson.value.videoLink || null }
      await api(editingLessonId.value ? `/lessons/${editingLessonId.value}` : '/lessons', {
        method: editingLessonId.value ? 'PUT' : 'POST',
        body: JSON.stringify(body),
      })
      const courseId = lesson.value.courseId
      resetLesson(courseId)
      await loadLessons(courseId)
    },
    editingLessonId.value ? 'Lesson updated.' : 'Lesson added.',
  )
}
function editLesson(item: Lesson) {
  lesson.value = { courseId: item.courseId, title: item.title, videoLink: item.videoLink ?? '' }
  editingLessonId.value = item.id
}
async function deleteLesson(id: string) {
  if (!window.confirm('Delete this lesson?')) return
  await submit(async () => {
    await api(`/lessons/${id}`, { method: 'DELETE' })
    if (editingLessonId.value === id) resetLesson()
    await loadLessons()
  }, 'Lesson deleted.')
}
function resetSection(courseId = section.value.courseId) {
  section.value = { courseId, title: '', description: '', orderIndex: '0' }
  editingSectionId.value = null
}
async function saveSection() {
  await submit(
    async () => {
      const body = {
        title: section.value.title,
        description: section.value.description || null,
        orderIndex: Number(section.value.orderIndex),
      }
      await api(
        editingSectionId.value
          ? `/sections/${editingSectionId.value}`
          : `/sections/course/${section.value.courseId}`,
        { method: editingSectionId.value ? 'PUT' : 'POST', body: JSON.stringify(body) },
      )
      const courseId = section.value.courseId
      resetSection(courseId)
      await loadSections(courseId)
    },
    editingSectionId.value ? 'Section updated.' : 'Section created.',
  )
}
function editSection(item: CourseSection) {
  section.value = {
    courseId: item.courseId,
    title: item.title,
    description: item.description ?? '',
    orderIndex: String(item.orderIndex),
  }
  editingSectionId.value = item.id
}
async function moveSection(item: CourseSection, direction: 'up' | 'down') {
  await submit(async () => {
    await api(`/sections/${item.id}/course/${item.courseId}/move-${direction}`, { method: 'PATCH' })
    await loadSections(item.courseId)
  }, `Section moved ${direction}.`)
}
async function saveSectionOrder() {
  await submit(async () => {
    await api(`/sections/course/${section.value.courseId}/reorder`, {
      method: 'PUT',
      body: JSON.stringify(sections.value.map((item) => item.id)),
    })
    await loadSections()
  }, 'Section order saved.')
}
async function deleteSection(item: CourseSection) {
  if (!window.confirm('Delete this section?')) return
  await submit(async () => {
    await api(`/sections/${item.id}/course/${item.courseId}`, { method: 'DELETE' })
    if (editingSectionId.value === item.id) resetSection(item.courseId)
    await loadSections(item.courseId)
  }, 'Section deleted.')
}
function resetProject() {
  project.value = { title: '', description: '', imagePath: '', projectUrl: '', displayOrder: '0' }
  editingProjectId.value = null
}
async function saveProject() {
  await submit(
    async () => {
      const body = {
        ...project.value,
        description: project.value.description || null,
        imagePath: project.value.imagePath || null,
        displayOrder: Number(project.value.displayOrder),
      }
      await api(editingProjectId.value ? `/projects/${editingProjectId.value}` : '/projects', {
        method: editingProjectId.value ? 'PUT' : 'POST',
        body: JSON.stringify(body),
      })
      resetProject()
      await loadPortfolio()
    },
    editingProjectId.value ? 'Project updated.' : 'Project added.',
  )
}
function editProject(item: Project) {
  project.value = {
    title: item.title,
    description: item.description ?? '',
    imagePath: item.imagePath ?? '',
    projectUrl: item.projectUrl,
    displayOrder: String(item.displayOrder),
  }
  editingProjectId.value = item.id
}
async function deleteProject(id: string) {
  if (!window.confirm('Delete this project?')) return
  await submit(async () => {
    await api(`/projects/${id}`, { method: 'DELETE' })
    if (editingProjectId.value === id) resetProject()
    await loadPortfolio()
  }, 'Project deleted.')
}
function resetCertificate() {
  certificate.value = { title: '', issuer: '', issuedDate: '', imagePath: '', displayOrder: '0' }
  editingCertificateId.value = null
}
async function saveCertificate() {
  await submit(
    async () => {
      const body = {
        ...certificate.value,
        issuer: certificate.value.issuer || null,
        issuedDate: certificate.value.issuedDate || null,
        displayOrder: Number(certificate.value.displayOrder),
      }
      await api(
        editingCertificateId.value
          ? `/certificates/${editingCertificateId.value}`
          : '/certificates',
        { method: editingCertificateId.value ? 'PUT' : 'POST', body: JSON.stringify(body) },
      )
      resetCertificate()
      await loadPortfolio()
    },
    editingCertificateId.value ? 'Certificate updated.' : 'Certificate added.',
  )
}
function editCertificate(item: Certificate) {
  certificate.value = {
    title: item.title,
    issuer: item.issuer ?? '',
    issuedDate: item.issuedDate ?? '',
    imagePath: item.imagePath,
    displayOrder: String(item.displayOrder),
  }
  editingCertificateId.value = item.id
}
async function deleteCertificate(id: string) {
  if (!window.confirm('Delete this certificate?')) return
  await submit(async () => {
    await api(`/certificates/${id}`, { method: 'DELETE' })
    if (editingCertificateId.value === id) resetCertificate()
    await loadPortfolio()
  }, 'Certificate deleted.')
}
function resetExperience() {
  experience.value = {
    company: '',
    role: '',
    startDate: '',
    endDate: '',
    summary: '',
    highlights: '',
    displayOrder: '0',
  }
  editingExperienceId.value = null
}
async function saveExperience() {
  await submit(
    async () => {
      const body = {
        ...experience.value,
        endDate: experience.value.endDate || null,
        highlights: experience.value.highlights
          .split('\n')
          .map((item) => item.trim())
          .filter(Boolean),
        displayOrder: Number(experience.value.displayOrder),
      }
      await api(
        editingExperienceId.value
          ? `/work-experiences/${editingExperienceId.value}`
          : '/work-experiences',
        { method: editingExperienceId.value ? 'PUT' : 'POST', body: JSON.stringify(body) },
      )
      resetExperience()
      await loadPortfolio()
    },
    editingExperienceId.value ? 'Experience updated.' : 'Experience added.',
  )
}
function editExperience(item: WorkExperience) {
  experience.value = {
    company: item.company,
    role: item.role,
    startDate: item.startDate,
    endDate: item.endDate ?? '',
    summary: item.summary,
    highlights: item.highlights.join('\n'),
    displayOrder: '0',
  }
  editingExperienceId.value = item.id
}
async function deleteExperience(id: string) {
  if (!window.confirm('Delete this work experience?')) return
  await submit(async () => {
    await api(`/work-experiences/${id}`, { method: 'DELETE' })
    if (editingExperienceId.value === id) resetExperience()
    await loadPortfolio()
  }, 'Experience deleted.')
}
function resetStudent() {
  student.value = { name: '', email: '', password: '' }
  editingStudentId.value = null
}
async function saveStudent() {
  await submit(
    async () => {
      const body = editingStudentId.value
        ? { name: student.value.name, email: student.value.email }
        : student.value
      await api(editingStudentId.value ? `/students/${editingStudentId.value}` : '/students', {
        method: editingStudentId.value ? 'PUT' : 'POST',
        body: JSON.stringify(body),
      })
      resetStudent()
      await loadStudents()
    },
    editingStudentId.value ? 'Student updated.' : 'Student account created.',
  )
}
function editStudent(item: Student) {
  student.value = { name: item.name, email: item.email, password: '' }
  editingStudentId.value = item.id
  selectedStudentId.value = item.id
}
async function deleteStudent(id: string) {
  if (!window.confirm('Delete this student account?')) return
  await submit(async () => {
    await api(`/students/${id}`, { method: 'DELETE' })
    if (editingStudentId.value === id) resetStudent()
    if (selectedStudentId.value === id) selectedStudentId.value = null
    await loadStudents()
  }, 'Student deleted.')
}
async function connectLinkedIn() {
  await submit(async () => {
    const result = await api<{ authorizationUrl: string }>('/linkedin/authorization-url', {
      method: 'POST',
    })
    globalThis.location.assign(result.authorizationUrl)
  }, 'Opening LinkedIn…')
}
onMounted(async () => {
  if (!authToken.value) return
  try {
    if (!(await verifyAdminAccess())) throw new Error()
    await Promise.all([
      loadProfile(),
      loadCourses(),
      loadArticles(),
      loadPortfolio(),
      loadStudents(),
      loadLinkedIn(),
    ])
  } catch {
    signOut()
  }
})
</script>

<template>
  <main class="min-h-svh bg-canvas text-ink">
    <section
      v-if="!isAdmin"
      class="mx-auto mt-[12vh] max-w-md rounded-3xl border border-ink/15 bg-white p-7 shadow-sm sm:p-10"
    >
      <p class="eyebrow">BashMobarmeg Academy</p>
      <h1 class="mt-4 font-serif text-5xl leading-none">Admin access</h1>
      <p class="mt-4 text-muted-ink">
        Sign in with the administrator account to manage Ryan’s site.
      </p>
      <form class="mt-8 space-y-4" @submit.prevent="login">
        <input v-model="email" class="field" type="email" placeholder="Email" required /><input
          v-model="password"
          class="field"
          type="password"
          placeholder="Password"
          required
          minlength="8"
        />
        <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
        <button class="primary-button w-full" :disabled="loading">
          {{ loading ? 'Signing in…' : 'Sign in' }}
        </button>
      </form>
    </section>
    <section v-else class="grid min-h-svh gap-0 lg:grid-cols-[17rem_minmax(0,1fr)]">
      <aside class="admin-sidebar">
        <RouterLink to="/about" class="brand">BashMobarmeg<span>Academy</span></RouterLink>
        <nav class="mt-10 grid gap-2" aria-label="Admin tools">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            class="tab-button"
            :class="activeTab === tab.id && 'tab-active'"
            @click="activeTab = tab.id; feedback()"
          >
            <span>{{ tab.icon }}</span>{{ tab.label }}
          </button>
        </nav>
        <button
          class="mt-auto text-left text-sm font-bold text-muted-ink hover:text-accent"
          @click="signOut"
        >
          Sign out
        </button>
      </aside>
      <div class="min-w-0 p-5 sm:p-7 lg:p-9">
        <header class="admin-topbar">
          <label class="admin-search"><span>⌕</span><input type="search" placeholder="Search anything…" aria-label="Search admin content" /></label>
          <div class="topbar-tools"><button type="button" aria-label="Notifications">♟</button><button type="button" aria-label="Toggle display">◐</button></div>
        </header>
        <header class="flex flex-wrap items-end justify-between gap-4 border-b border-ink/15 pb-6 pt-7">
          <div>
            <p class="eyebrow">Management workspace</p>
            <h1 class="mt-3 font-serif text-5xl tracking-[-.06em]">{{ activeTitle }}</h1>
          </div>
          <p class="max-w-sm text-sm leading-relaxed text-muted-ink">
            Manage the single Ryan profile, global content, and student accounts.
          </p>
        </header>
        <p v-if="message" class="feedback success">{{ message }}</p>
        <p v-if="error" class="feedback failure">{{ error }}</p>
        <section v-if="activeTab === 'dashboard'" class="dashboard-view mt-8 space-y-6">
          <div class="dashboard-welcome"><div><p class="eyebrow">Dashboard</p><h2>Good morning, Ryan <span>☀</span></h2><p>Here’s what’s happening across your platform today.</p></div><div class="quick-actions"><b>Quick actions</b><button type="button" @click="activeTab = 'course'; selectedCourseId = null; resetCourse()">＋ Add course</button><button type="button" @click="activeTab = 'article'; resetArticle()">＋ Add article</button><button type="button" @click="activeTab = 'project'; resetProject()">＋ Add project</button></div></div>
          <div class="stats-grid">
            <button class="stat-card" @click="activeTab = 'student'"><span class="stat-icon">♙</span><span>Total students</span><b>{{ students.length }}</b><small>Live student accounts</small></button>
            <button class="stat-card" @click="activeTab = 'course'"><span class="stat-icon">▣</span><span>Active courses</span><b>{{ activeCourses.length }}</b><small>{{ completedCourses.length }} completed</small></button>
            <button class="stat-card" @click="activeTab = 'article'"><span class="stat-icon">✦</span><span>Total articles</span><b>{{ articles.length }}</b><small>Published content</small></button>
            <button class="stat-card" @click="activeTab = 'project'"><span class="stat-icon">↗</span><span>Total projects</span><b>{{ projects.length }}</b><small>Portfolio work</small></button>
          </div>
          <div class="dashboard-panels">
            <section class="editor-card dashboard-list"><div class="panel-heading"><div><p class="eyebrow">Learners</p><h2>Recent students</h2></div><button type="button" @click="activeTab = 'student'">View all</button></div><div v-if="students.length" class="compact-list"><button v-for="item in students.slice(0, 5)" :key="item.id" type="button" @click="activeTab = 'student'"><span class="avatar-placeholder">{{ item.name.slice(0, 1) }}</span><span><b>{{ item.name }}</b><small>{{ item.email }}</small></span><em>Active</em></button></div><p v-else class="empty-copy">No student accounts yet.</p></section>
            <section class="editor-card dashboard-list"><div class="panel-heading"><div><p class="eyebrow">Learning</p><h2>Active courses</h2></div><button type="button" @click="activeTab = 'course'">View all</button></div><div v-if="activeCourses.length" class="compact-list"><button v-for="item in activeCourses.slice(0, 5)" :key="item.id" type="button" @click="activeTab = 'course'; openCourse(item)"><span class="course-thumb">▣</span><span><b>{{ item.title }}</b><small>{{ item.price === 0 ? 'Free course' : `$${item.price}` }}</small></span><em>Open</em></button></div><p v-else class="empty-copy">No active courses yet.</p></section>
            <section class="editor-card dashboard-list"><div class="panel-heading"><div><p class="eyebrow">Publishing</p><h2>Latest articles</h2></div><button type="button" @click="activeTab = 'article'">View all</button></div><div v-if="articles.length" class="compact-list"><button v-for="item in articles.slice(0, 5)" :key="item.id" type="button" @click="activeTab = 'article'; editArticle(item)"><span class="course-thumb">✦</span><span><b>{{ item.title }}</b><small>{{ item.tags.join(' · ') || 'Untitled category' }}</small></span><em>Edit</em></button></div><p v-else class="empty-copy">No articles yet.</p></section>
          </div>
        </section>
        <form
          v-else-if="activeTab === 'profile'"
          class="profile-workspace mt-8 space-y-4"
          @submit.prevent="saveProfile"
        >
          <section class="profile-preview editor-card"><p class="eyebrow">Public profile</p><div class="profile-identity"><img v-if="profile.photoUrl" :src="profile.photoUrl" alt="Ryan profile" /><span v-else>{{ profile.name.slice(0, 1) || 'R' }}</span><div><h2>{{ profile.name || 'Ryan' }}</h2><p>{{ profile.headline || 'Your professional headline' }}</p><small>This preview powers the public About page.</small></div></div><p class="profile-bio">{{ profile.biography || 'Add a biography to introduce your work and perspective.' }}</p></section>
          <section class="editor-card profile-form"><div><p class="eyebrow">About Ryan</p><h2 class="mt-1 font-serif text-2xl">Profile details</h2></div><input v-model="profile.name" class="field" placeholder="Name" required /><input
            v-model="profile.headline"
            class="field"
            placeholder="Headline"
          /><input v-model="profile.photoUrl" class="field" placeholder="Photo URL" /><label
            class="upload-field"
            >Upload a JPG, PNG, or WebP photo (max 5 MB)<input
              accept="image/jpeg,image/png,image/webp"
              type="file"
              :disabled="loading"
              @change="uploadProfilePhoto" /></label
          ><textarea
            v-model="profile.biography"
            class="field min-h-36"
            placeholder="Biography"
          ></textarea
          ><button class="primary-button" :disabled="loading">Save profile</button></section>
        </form>
        <section v-else-if="activeTab === 'article'" class="article-workspace mt-8">
          <div class="article-list editor-card"><div class="flex items-start justify-between gap-3"><div><p class="eyebrow">Publishing</p><h2 class="mt-1 font-serif text-2xl">Articles</h2><p class="mt-1 text-xs text-muted-ink">Manage your long-form content.</p></div><button class="primary-button" type="button" @click="resetArticle">＋ Add article</button></div><label class="student-search"><span>⌕</span><input v-model="articleQuery" type="search" placeholder="Search articles by title…" /></label><div v-if="visibleArticles.length" class="article-list-items"><button v-for="item in visibleArticles" :key="item.id" type="button" :class="editingArticleId === item.id && 'article-selected'" @click="editArticle(item)"><span class="course-thumb">✦</span><span><b>{{ item.title }}</b><small>{{ item.tags.join(' · ') || 'No tags' }}</small></span><em>Published</em></button></div><p v-else class="empty-copy">{{ articles.length ? 'No articles match this search.' : 'No articles yet.' }}</p></div>
          <form class="editor-card article-editor grid gap-4" @submit.prevent="saveArticle">
            <div class="flex items-start justify-between gap-3"><div><p class="eyebrow">{{ editingArticleId ? 'Article editor' : 'New article' }}</p><h2 class="mt-1 font-serif text-2xl">{{ editingArticleId ? 'Edit article' : 'Write an article' }}</h2></div><button v-if="editingArticleId" class="secondary-button" type="button" @click="resetArticle">New article</button></div>
            <input
              v-model="article.title"
              class="field"
              placeholder="Article title"
              required
            /><input
              v-model="article.tags"
              class="field"
              placeholder="Tags, separated by commas"
            /><textarea
              v-model="article.content"
              class="field min-h-80"
              placeholder="Markdown content"
              required
            ></textarea>
            <div class="flex gap-3">
              <button class="primary-button" :disabled="loading">
                {{ editingArticleId ? 'Save article' : 'Publish article' }}</button
              ><button
                v-if="editingArticleId"
                class="secondary-button"
                type="button"
                @click="resetArticle"
              >
                Cancel
              </button>
            </div>
          </form>
          <button v-if="editingArticleId" class="danger-button justify-self-start" type="button" @click="deleteArticle(editingArticleId)">Delete article</button>
        </section>
        <section v-else-if="activeTab === 'course'" class="course-workspace mt-8">
          <div class="course-library editor-card">
            <div><p class="eyebrow">Course library</p><h2 class="mt-1 font-serif text-2xl">Courses</h2><p class="mt-1 text-xs text-muted-ink">Manage and organize all courses.</p></div>
            <div class="course-library-actions"><label><span>⌕</span><input v-model="courseQuery" type="search" placeholder="Search courses" /></label><button class="primary-button" type="button" @click="selectedCourseId = null; resetCourse()">＋ Add course</button></div>
            <div class="course-status-summary"><span><b>{{ activeCourses.length }}</b> active</span><span><b>{{ completedCourses.length }}</b> completed</span><span><b>{{ courses.filter((item) => item.status === 'ARCHIVED').length }}</b> archived</span></div>
            <div v-if="visibleCourses.length" class="course-library-list mt-5">
              <button v-for="item in visibleCourses" :key="item.id" class="course-library-item" :class="selectedCourseId === item.id && 'course-library-item-active'" type="button" @click="openCourse(item)"><span class="course-library-thumb">▣</span><span><b>{{ item.title }}</b><small>{{ item.enrollments?.length ?? 0 }} students · {{ item.price === 0 ? 'Free' : `$${item.price}` }}</small></span><em :class="`status-${item.status.toLowerCase()}`">{{ item.status.toLowerCase() }}</em></button>
            </div>
            <p v-else class="mt-5 text-sm text-muted-ink">{{ courses.length ? 'No courses match this search.' : 'Your course library is empty.' }}</p>
          </div>
          <div class="min-w-0 space-y-6">
            <div v-if="selectedCourse" class="workspace-banner"><div><p class="eyebrow">Course workspace</p><h2 class="mt-1 font-serif text-3xl">{{ selectedCourse.title }}</h2><p class="mt-2 text-sm text-muted-ink">Manage course details, lessons, and sections in one place.</p></div><span :class="`status-${selectedCourse.status.toLowerCase()}`">{{ selectedCourse.status.toLowerCase() }}</span></div>
            <form class="editor-card grid gap-4 sm:grid-cols-2" @submit.prevent="saveCourse">
            <p class="text-sm text-muted-ink sm:col-span-2">{{ selectedCourse ? 'Course details' : 'Create a new course to begin building its content.' }}</p>
            <input
              v-model="course.title"
              class="field sm:col-span-2"
              placeholder="Course title"
              required
            /><input
              v-model="course.price"
              class="field"
              type="number"
              min="0"
              step=".01"
              placeholder="Price"
              required
            /><select v-model="course.status" class="field"><option value="ACTIVE">Active</option><option value="COMPLETED">Completed</option><option value="ARCHIVED">Archived</option></select><input
              v-model="course.thumbnailLink"
              class="field"
              placeholder="Thumbnail URL"
            /><textarea
              v-model="course.description"
              class="field min-h-44 sm:col-span-2"
              placeholder="Course description"
              required
            ></textarea>
            <div class="flex gap-3">
              <button class="primary-button" :disabled="loading">
                {{ editingCourseId ? 'Save course' : 'Create course' }}</button
              ><button
                v-if="editingCourseId"
                class="secondary-button"
                type="button"
                @click="resetCourse"
              >
                Cancel
              </button>
            </div>
          </form>
            <template v-if="selectedCourse">
              <section class="editor-card">
                <div class="flex flex-wrap items-center justify-between gap-3"><div><p class="eyebrow">Learning content</p><h3 class="mt-1 font-serif text-2xl">Lessons</h3></div><span class="text-sm text-muted-ink">{{ lessons.length }} total</span></div>
                <form class="mt-5 grid gap-3 sm:grid-cols-[1fr_1fr_auto]" @submit.prevent="saveLesson"><input v-model="lesson.title" class="field" placeholder="Lesson title" required /><input v-model="lesson.videoLink" class="field" type="url" placeholder="Video URL (optional)" /><button class="primary-button" :disabled="loading">{{ editingLessonId ? 'Save lesson' : 'Add lesson' }}</button></form>
                <button v-if="editingLessonId" class="mt-3 text-sm font-bold text-accent" type="button" @click="resetLesson(selectedCourse.id)">Cancel lesson edit</button>
                <div v-if="lessons.length" class="mt-5 space-y-2"><article v-for="item in lessons" :key="item.id" class="manage-row"><span><b>{{ item.title }}</b><small>{{ item.videoLink || 'No video link' }}</small></span><span class="flex gap-2"><button class="secondary-button" type="button" @click="editLesson(item)">Edit</button><button class="danger-button" type="button" @click="deleteLesson(item.id)">Delete</button></span></article></div><p v-else class="mt-5 text-sm text-muted-ink">No lessons yet. Add the first one above.</p>
              </section>
              <section class="editor-card">
                <div class="flex flex-wrap items-center justify-between gap-3"><div><p class="eyebrow">Organization</p><h3 class="mt-1 font-serif text-2xl">Sections</h3></div><button v-if="sections.length" class="secondary-button" type="button" @click="saveSectionOrder">Save order</button></div>
                <form class="mt-5 grid gap-3 sm:grid-cols-[1fr_7rem]" @submit.prevent="saveSection"><input v-model="section.title" class="field" placeholder="Section title" required /><input v-model="section.orderIndex" class="field" type="number" min="0" placeholder="Order" required /><textarea v-model="section.description" class="field min-h-24 sm:col-span-2" placeholder="Description (optional)"></textarea><div><button class="primary-button" :disabled="loading">{{ editingSectionId ? 'Save section' : 'Add section' }}</button><button v-if="editingSectionId" class="secondary-button ml-2" type="button" @click="resetSection(selectedCourse.id)">Cancel</button></div></form>
                <div v-if="sections.length" class="mt-5 space-y-2"><article v-for="item in sections" :key="item.id" class="manage-row"><span><b>{{ item.orderIndex }}. {{ item.title }}</b><small>{{ item.description || 'No description' }}</small></span><span class="flex gap-2"><button class="secondary-button" type="button" @click="moveSection(item, 'up')">↑</button><button class="secondary-button" type="button" @click="moveSection(item, 'down')">↓</button><button class="secondary-button" type="button" @click="editSection(item)">Edit</button><button class="danger-button" type="button" @click="deleteSection(item)">Delete</button></span></article></div><p v-else class="mt-5 text-sm text-muted-ink">No sections yet. Add a section to give this course structure.</p>
              </section>
              <button class="danger-button" type="button" @click="deleteCourse(selectedCourse.id)">Delete this course</button>
            </template>
          </div>
        </section>
        <section v-else-if="activeTab === 'lesson'" class="mt-8 max-w-4xl">
          <form class="editor-card grid gap-4 sm:grid-cols-2" @submit.prevent="saveLesson">
            <select
              v-model="lesson.courseId"
              class="field sm:col-span-2"
              required
              @change="loadLessons()"
            >
              <option value="" disabled>Select course</option>
              <option v-for="item in courses" :key="item.id" :value="item.id">
                {{ item.title }}
              </option></select
            ><input
              v-model="lesson.title"
              class="field sm:col-span-2"
              placeholder="Lesson title"
              required
            /><input
              v-model="lesson.videoLink"
              class="field sm:col-span-2"
              type="url"
              placeholder="Video URL (optional)"
            />
            <div class="flex gap-3">
              <button class="primary-button" :disabled="loading || !courses.length">
                {{ editingLessonId ? 'Save lesson' : 'Add lesson' }}</button
              ><button
                v-if="editingLessonId"
                class="secondary-button"
                type="button"
                @click="resetLesson()"
              >
                Cancel
              </button>
            </div>
          </form>
          <div class="editor-card mt-6">
            <h2 class="font-serif text-2xl">Lessons</h2>
            <p v-if="!lesson.courseId" class="mt-4 text-sm text-muted-ink">
              Select a course to manage its lessons.
            </p>
            <div v-else-if="lessons.length" class="mt-4 space-y-3">
              <article v-for="item in lessons" :key="item.id" class="manage-row">
                <span
                  ><b>{{ item.title }}</b
                  ><small>{{ item.videoLink || 'No video link' }}</small></span
                ><span class="flex gap-2"
                  ><button class="secondary-button" type="button" @click="editLesson(item)">
                    Edit</button
                  ><button class="danger-button" type="button" @click="deleteLesson(item.id)">
                    Delete
                  </button></span
                >
              </article>
            </div>
            <p v-else class="mt-4 text-sm text-muted-ink">No lessons in this course.</p>
          </div>
        </section>
        <section v-else-if="activeTab === 'section'" class="mt-8 max-w-4xl">
          <form class="editor-card grid gap-4 sm:grid-cols-2" @submit.prevent="saveSection">
            <select
              v-model="section.courseId"
              class="field sm:col-span-2"
              required
              @change="resetSection(section.courseId); loadSections()"
            >
              <option value="" disabled>Select course</option>
              <option v-for="item in courses" :key="item.id" :value="item.id">
                {{ item.title }}
              </option></select
            ><input
              v-model="section.title"
              class="field sm:col-span-2"
              placeholder="Section title"
              required
            /><textarea
              v-model="section.description"
              class="field min-h-28 sm:col-span-2"
              placeholder="Description (optional)"
            ></textarea
            ><input
              v-model="section.orderIndex"
              class="field"
              type="number"
              min="0"
              placeholder="Order"
              required
            />
            <div class="flex gap-3">
              <button class="primary-button" :disabled="loading || !section.courseId">
                {{ editingSectionId ? 'Save section' : 'Create section' }}</button
              ><button
                v-if="editingSectionId"
                class="secondary-button"
                type="button"
                @click="resetSection()"
              >
                Cancel
              </button>
            </div>
          </form>
          <div class="editor-card mt-6">
            <div class="flex items-center justify-between gap-4">
              <h2 class="font-serif text-2xl">Course sections</h2>
              <button
                v-if="sections.length"
                class="secondary-button"
                type="button"
                @click="saveSectionOrder"
              >
                Save order
              </button>
            </div>
            <p v-if="!section.courseId" class="mt-4 text-sm text-muted-ink">
              Select a course to manage its sections.
            </p>
            <div v-else-if="sections.length" class="mt-4 space-y-3">
              <article v-for="item in sections" :key="item.id" class="manage-row">
                <span
                  ><b>{{ item.orderIndex }}. {{ item.title }}</b
                  ><small>{{ item.description || 'No description' }}</small></span
                ><span class="flex gap-2"
                  ><button class="secondary-button" type="button" @click="editSection(item)">
                    Edit</button
                  ><button class="secondary-button" type="button" @click="moveSection(item, 'up')">
                    ↑</button
                  ><button
                    class="secondary-button"
                    type="button"
                    @click="moveSection(item, 'down')"
                  >
                    ↓</button
                  ><button class="danger-button" type="button" @click="deleteSection(item)">
                    Delete
                  </button></span
                >
              </article>
            </div>
            <p v-else class="mt-4 text-sm text-muted-ink">No sections in this course.</p>
          </div>
        </section>
        <section v-else-if="activeTab === 'project'" class="manage-workspace project-management mt-8">
          <form class="editor-card grid gap-4 sm:grid-cols-2" @submit.prevent="saveProject">
            <input
              v-model="project.title"
              class="field sm:col-span-2"
              placeholder="Project title"
              required
            /><input
              v-model="project.projectUrl"
              class="field"
              type="url"
              placeholder="Project URL"
              required
            /><input v-model="project.imagePath" class="field" placeholder="Image URL" /><input
              v-model="project.displayOrder"
              class="field"
              type="number"
              min="0"
              placeholder="Display order"
              required
            /><textarea
              v-model="project.description"
              class="field min-h-36 sm:col-span-2"
              placeholder="Project description"
            ></textarea>
            <div class="flex gap-3">
              <button class="primary-button" :disabled="loading">
                {{ editingProjectId ? 'Save project' : 'Add project' }}</button
              ><button
                v-if="editingProjectId"
                class="secondary-button"
                type="button"
                @click="resetProject"
              >
                Cancel
              </button>
            </div>
          </form>
          <div class="editor-card mt-6">
            <h2 class="font-serif text-2xl">Projects</h2>
            <div v-if="projects.length" class="mt-4 space-y-3">
              <article v-for="item in projects" :key="item.id" class="manage-row">
                <span
                  ><b>{{ item.title }}</b
                  ><small>{{ item.projectUrl }}</small></span
                ><span class="flex gap-2"
                  ><button class="secondary-button" type="button" @click="editProject(item)">
                    Edit</button
                  ><button class="danger-button" type="button" @click="deleteProject(item.id)">
                    Delete
                  </button></span
                >
              </article>
            </div>
            <p v-else class="mt-4 text-sm text-muted-ink">No projects yet.</p>
          </div>
        </section>
        <section v-else-if="activeTab === 'certificate'" class="manage-workspace certificate-management mt-8">
          <form class="editor-card grid gap-4 sm:grid-cols-2" @submit.prevent="saveCertificate">
            <input
              v-model="certificate.title"
              class="field"
              placeholder="Certificate title"
              required
            /><input v-model="certificate.issuer" class="field" placeholder="Issuer" /><input
              v-model="certificate.issuedDate"
              class="field"
              type="date"
            /><input
              v-model="certificate.displayOrder"
              class="field"
              type="number"
              min="0"
              placeholder="Display order"
              required
            /><input
              v-model="certificate.imagePath"
              class="field sm:col-span-2"
              placeholder="Certificate image URL"
              required
            />
            <div class="flex gap-3">
              <button class="primary-button" :disabled="loading">
                {{ editingCertificateId ? 'Save certificate' : 'Add certificate' }}</button
              ><button
                v-if="editingCertificateId"
                class="secondary-button"
                type="button"
                @click="resetCertificate"
              >
                Cancel
              </button>
            </div>
          </form>
          <div class="editor-card mt-6">
            <h2 class="font-serif text-2xl">Certificates</h2>
            <div v-if="certificates.length" class="mt-4 space-y-3">
              <article v-for="item in certificates" :key="item.id" class="manage-row">
                <span
                  ><b>{{ item.title }}</b
                  ><small>{{ item.issuer || 'No issuer' }}</small></span
                ><span class="flex gap-2"
                  ><button class="secondary-button" type="button" @click="editCertificate(item)">
                    Edit</button
                  ><button class="danger-button" type="button" @click="deleteCertificate(item.id)">
                    Delete
                  </button></span
                >
              </article>
            </div>
            <p v-else class="mt-4 text-sm text-muted-ink">No certificates yet.</p>
          </div>
        </section>
        <section v-else-if="activeTab === 'experience'" class="manage-workspace experience-management mt-8">
          <form class="editor-card grid gap-4 sm:grid-cols-2" @submit.prevent="saveExperience">
            <input
              v-model="experience.company"
              class="field"
              placeholder="Company"
              required
            /><input v-model="experience.role" class="field" placeholder="Role" required /><input
              v-model="experience.startDate"
              class="field"
              type="date"
              required
            /><input v-model="experience.endDate" class="field" type="date" /><textarea
              v-model="experience.summary"
              class="field min-h-28 sm:col-span-2"
              placeholder="Summary"
              required
            ></textarea
            ><textarea
              v-model="experience.highlights"
              class="field min-h-28 sm:col-span-2"
              placeholder="Highlights, one per line"
            ></textarea
            ><input
              v-model="experience.displayOrder"
              class="field"
              type="number"
              min="0"
              placeholder="Display order"
            />
            <div class="flex gap-3">
              <button class="primary-button" :disabled="loading">
                {{ editingExperienceId ? 'Save experience' : 'Add experience' }}</button
              ><button
                v-if="editingExperienceId"
                class="secondary-button"
                type="button"
                @click="resetExperience"
              >
                Cancel
              </button>
            </div>
          </form>
          <div class="editor-card mt-6">
            <h2 class="font-serif text-2xl">Work experience</h2>
            <div v-if="experiences.length" class="mt-4 space-y-3">
              <article v-for="item in experiences" :key="item.id" class="manage-row">
                <span
                  ><b>{{ item.role }} · {{ item.company }}</b
                  ><small>{{ item.startDate }} – {{ item.endDate || 'Present' }}</small></span
                ><span class="flex gap-2"
                  ><button class="secondary-button" type="button" @click="editExperience(item)">
                    Edit</button
                  ><button class="danger-button" type="button" @click="deleteExperience(item.id)">
                    Delete
                  </button></span
                >
              </article>
            </div>
            <p v-else class="mt-4 text-sm text-muted-ink">No work experience yet.</p>
          </div>
        </section>
        <section v-else-if="activeTab === 'student'" class="student-workspace mt-8">
          <div class="student-roster editor-card">
            <div class="flex flex-wrap items-start justify-between gap-3"><div><p class="eyebrow">Learner management</p><h2 class="mt-1 font-serif text-2xl">Students</h2><p class="mt-1 text-xs text-muted-ink">Manage and view all student accounts.</p></div><button class="primary-button" type="button" @click="selectedStudentId = null; resetStudent()">＋ Add student</button></div>
            <label class="student-search"><span>⌕</span><input v-model="studentQuery" type="search" placeholder="Search students by name or email…" /></label>
            <div v-if="visibleStudents.length" class="student-table-wrap"><table class="student-table"><thead><tr><th>#</th><th>Student</th><th>Courses</th><th>Status</th><th></th></tr></thead><tbody><tr v-for="(item, index) in visibleStudents" :key="item.id" :class="selectedStudentId === item.id && 'student-row-active'" @click="selectedStudentId = item.id; resetStudent()"><td>{{ index + 1 }}</td><td><span class="student-avatar">{{ item.name.slice(0, 1) }}</span><span><b>{{ item.name }}</b><small>{{ item.email }}</small></span></td><td>{{ studentEnrollmentCount(item.id) }}</td><td><em>Active</em></td><td><button class="row-action" type="button" @click.stop="editStudent(item)">•••</button></td></tr></tbody></table></div><p v-else class="empty-copy">{{ students.length ? 'No students match this search.' : 'No student accounts yet.' }}</p>
          </div>
          <aside class="student-detail editor-card">
            <template v-if="selectedStudent && !editingStudentId">
              <div class="student-detail-head"><span class="student-profile-avatar">{{ selectedStudent.name.slice(0, 1) }}</span><div><p class="eyebrow">Student profile</p><h2>{{ selectedStudent.name }}</h2><small>{{ selectedStudent.email }}</small><em>Active</em></div><button class="secondary-button" type="button" @click="editStudent(selectedStudent)">Edit</button></div>
              <div class="student-facts"><span><b>{{ selectedStudentEnrollments.length }}</b> courses</span><span><b>—</b> progress</span><span><b>—</b> reviews</span></div>
              <div class="detail-section"><p class="eyebrow">Enrolled courses</p><div v-if="selectedStudentEnrollments.length" class="enrollment-list"><article v-for="enrollment in selectedStudentEnrollments" :key="enrollment.id"><span class="course-thumb">▣</span><span><b>{{ enrollment.courseTitle }}</b><small>{{ enrollment.paymentStatus }} · {{ new Date(enrollment.enrolledAt).toLocaleDateString() }}</small></span></article></div><p v-else class="empty-copy">This student has no course enrollments yet.</p></div>
              <p class="student-data-note">Progress and reviews are intentionally unavailable until the backend scopes those endpoints to the signed-in user.</p>
            </template>
            <form v-else class="space-y-4" @submit.prevent="saveStudent"><div><p class="eyebrow">{{ editingStudentId ? 'Student profile' : 'New learner' }}</p><h2 class="mt-1 font-serif text-2xl">{{ editingStudentId ? 'Edit student' : 'Add student' }}</h2><p class="mt-1 text-xs text-muted-ink">{{ editingStudentId ? 'Update this student account.' : 'Only student accounts are created here.' }}</p></div><input v-model="student.name" class="field" placeholder="Full name" required /><input v-model="student.email" class="field" type="email" placeholder="Email address" required /><input v-if="!editingStudentId" v-model="student.password" class="field" type="password" minlength="8" placeholder="Temporary password" required /><div class="flex gap-2"><button class="primary-button flex-1" :disabled="loading">{{ editingStudentId ? 'Save changes' : 'Create student' }}</button><button v-if="editingStudentId" class="secondary-button" type="button" @click="resetStudent()">Cancel</button></div></form>
          </aside>
        </section>
        <section v-else class="editor-card mt-8 max-w-2xl">
          <p
            v-if="linkedIn?.connected"
            class="rounded-xl bg-emerald-500/10 p-4 text-sm text-emerald-300"
          >
            <b>Connected</b><br />{{ linkedIn.personUrn }}
          </p>
          <p v-else class="rounded-xl bg-amber-500/10 p-4 text-sm text-amber-300">
            <b>{{ linkedIn?.configured ? 'Not connected' : 'Configuration required' }}</b>
          </p>
          <button
            class="primary-button mt-5"
            :disabled="loading || !linkedIn?.configured"
            @click="connectLinkedIn"
          >
            Connect LinkedIn
          </button>
        </section>
      </div>
    </section>
  </main>
</template>

<style scoped>
.eyebrow {
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--color-accent);
}
.admin-sidebar,
.editor-card {
  border: 1px solid color-mix(in srgb, var(--color-ink) 14%, transparent);
  border-radius: 1.5rem;
  background: white;
  padding: 1.25rem;
  box-shadow: 0 1px 3px rgb(16 32 51 / 8%);
}
.admin-sidebar {
  display: flex;
  min-height: 100svh;
  flex-direction: column;
  border: 0;
  border-radius: 0;
  background: linear-gradient(160deg, #071a31, #0b2745);
  box-shadow: 0 18px 45px rgb(6 28 52 / 25%);
  color: #d8e8fa;
}
.brand {
  font-family: 'Cairo', sans-serif;
  font-size: 1.3rem;
  font-weight: 800;
  letter-spacing: -0.05em;
  color: #fff;
}
.brand span {
  color: var(--color-accent);
}
.tab-button {
  border-radius: 0.8rem;
  padding: 0.8rem 0.9rem;
  text-align: left;
  font-size: 0.9rem;
  font-weight: 700;
  color: #c3d1e1;
}
.tab-button:hover {
  background: rgb(255 255 255 / 8%);
  color: #fff;
}
.tab-active,
.primary-button {
  background: #087cff;
  color: #fff;
}
.admin-topbar{display:flex;align-items:center;justify-content:space-between;gap:1rem;border-bottom:1px solid color-mix(in srgb,var(--color-ink) 10%,transparent);padding:.25rem 0 1rem}.admin-search{display:flex;align-items:center;gap:.45rem;width:min(29rem,100%);border:1px solid #dbe7f4;border-radius:.55rem;background:#fff;padding:.45rem .7rem;color:#4380bd}.admin-search input{width:100%;border:0;background:transparent;outline:0;font-size:.78rem}.topbar-tools{display:flex;gap:.35rem}.topbar-tools button{display:grid;width:2rem;height:2rem;place-items:center;border:1px solid #dbe7f4;border-radius:.5rem;background:#fff;color:#0d3159}.dashboard-welcome{display:flex;align-items:stretch;justify-content:space-between;gap:1rem}.dashboard-welcome h2{margin:.25rem 0;font-size:1.75rem;font-weight:800;letter-spacing:-.04em}.dashboard-welcome h2 span{font-size:1.15rem}.dashboard-welcome>div>p:last-child{font-size:.82rem;color:var(--color-muted-ink)}.quick-actions{display:grid;grid-template-columns:repeat(3,auto);gap:.45rem;align-items:center;border:1px solid #e1eaf3;border-radius:1rem;background:#fff;padding:.65rem .75rem;box-shadow:0 5px 18px rgb(0 0 0 / 18%)}.quick-actions b{grid-column:1/-1;font-size:.72rem}.quick-actions button,.panel-heading button{border-radius:.45rem;background:#eff7ff;padding:.38rem .55rem;color:#087cff;font-size:.7rem;font-weight:800}.stats-grid{grid-template-columns:repeat(4,minmax(0,1fr))}.stat-card{position:relative;border-color:#e1eaf3;border-radius:1rem;box-shadow:0 5px 18px rgb(0 0 0 / 18%)}.stat-card .stat-icon{display:inline-grid;width:1.7rem;height:1.7rem;place-items:center;border-radius:.45rem;background:#eef7ff;color:#087cff}.stat-card b{font-family:inherit;font-size:1.7rem}.dashboard-panels{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:1rem}.dashboard-list{padding:1rem}.panel-heading{display:flex;align-items:start;justify-content:space-between;gap:.5rem;border-bottom:1px solid color-mix(in srgb,var(--color-ink) 12%,transparent);padding-bottom:.8rem}.panel-heading h2{margin-top:.15rem;font-size:1rem;font-weight:800}.compact-list{display:grid}.compact-list button{display:grid;grid-template-columns:auto 1fr auto;align-items:center;gap:.55rem;border-bottom:1px solid color-mix(in srgb,var(--color-ink) 10%,transparent);padding:.72rem 0;text-align:left}.compact-list button:last-child{border-bottom:0}.compact-list b,.compact-list small{display:block}.compact-list b{font-size:.74rem}.compact-list small{margin-top:.15rem;overflow:hidden;color:var(--color-muted-ink);font-size:.65rem;text-overflow:ellipsis;white-space:nowrap}.compact-list em{border-radius:999px;background:#dcfce7;padding:.15rem .35rem;color:#16803d;font-size:.6rem;font-style:normal;font-weight:800}.avatar-placeholder,.course-thumb{display:grid;width:1.8rem;height:1.8rem;place-items:center;border-radius:50%;background:#dbeeff;color:#087cff;font-size:.72rem;font-weight:800}.course-thumb{border-radius:.4rem;background:#e9f3ff}.empty-copy{padding:.9rem 0;font-size:.78rem;color:var(--color-muted-ink)}
.field {
  width: 100%;
  border: 1px solid color-mix(in srgb, var(--color-ink) 18%, transparent);
  border-radius: 0.8rem;
  background: #fff;
  padding: 0.8rem 1rem;
  color: var(--color-ink);
}
.upload-field {
  display: block;
  border: 1px dashed var(--color-primary);
  border-radius: 0.8rem;
  padding: 0.8rem 1rem;
  color: var(--color-accent);
  font-size: 0.875rem;
  font-weight: 700;
}
.upload-field input {
  display: block;
  margin-top: 0.5rem;
  width: 100%;
  color: var(--color-muted-ink);
}
.primary-button,
.secondary-button,
.danger-button {
  border-radius: 0.8rem;
  padding: 0.65rem 0.9rem;
  font-size: 0.85rem;
  font-weight: 700;
}
.primary-button:disabled {
  opacity: 0.6;
}
.secondary-button {
  border: 1px solid color-mix(in srgb, var(--color-ink) 20%, transparent);
  background: white;
  color: var(--color-ink);
}
.danger-button {
  background: color-mix(in srgb, #ef4444 20%, var(--color-surface));
  color: #fca5a5;
}
.manage-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  border: 1px solid color-mix(in srgb, var(--color-ink) 12%, transparent);
  border-radius: 0.8rem;
  padding: 0.85rem;
}
.manage-row b,
.manage-row small {
  display: block;
}
.manage-row small {
  margin-top: 0.2rem;
  color: var(--color-muted-ink);
  font-size: 0.75rem;
  overflow-wrap: anywhere;
}
.feedback {
  margin-top: 1.5rem;
  border-radius: 0.8rem;
  padding: 0.8rem 1rem;
  font-size: 0.9rem;
}
.course-workspace{display:grid;gap:1.5rem;grid-template-columns:minmax(18rem,23rem) minmax(0,1fr)}
.course-library{align-self:start;position:sticky;top:1rem}.course-library-actions{display:flex;gap:.5rem;margin-top:1rem}.course-library-actions label{display:flex;min-width:0;flex:1;align-items:center;gap:.3rem;border:1px solid #dbe7f4;border-radius:.55rem;padding:.42rem .55rem;color:#4380bd}.course-library-actions input{min-width:0;width:100%;border:0;outline:0;font-size:.72rem}.course-library-actions .primary-button{padding:.45rem .6rem;white-space:nowrap}.course-status-summary{display:flex;gap:.35rem;flex-wrap:wrap;margin-top:.75rem}.course-status-summary span{border-radius:999px;background:#f2f7fc;padding:.22rem .45rem;color:#66809c;font-size:.64rem}.course-status-summary b{color:#0f3158}.course-library-list{display:grid;gap:.42rem}.course-library-item{display:grid;grid-template-columns:auto 1fr auto;align-items:center;gap:.65rem;border:1px solid transparent;border-radius:.75rem;padding:.7rem;text-align:left}.course-library-item:hover{background:color-mix(in srgb,var(--color-primary) 6%,var(--color-surface))}.course-library-item-active{border-color:color-mix(in srgb,var(--color-primary) 45%,transparent);background:color-mix(in srgb,var(--color-primary) 9%,var(--color-surface))}.course-library-item b,.course-library-item small{display:block}.course-library-item b{font-size:.76rem}.course-library-item small{margin-top:.18rem;color:var(--color-muted-ink);font-size:.66rem}.course-library-thumb{display:grid;width:1.9rem;height:1.9rem;place-items:center;border-radius:.45rem;background:#e9f3ff;color:#087cff;font-size:.75rem}.workspace-banner{display:flex;align-items:flex-start;justify-content:space-between;gap:1rem;border-radius:1.25rem;background:linear-gradient(130deg,#09254a,#0c3c70);padding:1.35rem;color:#fff}.workspace-banner .eyebrow{color:#b9e5d5}.workspace-banner .text-muted-ink{color:#d3dbe0}.status-active,.status-completed,.status-archived{display:inline-flex;align-items:center;border-radius:999px;padding:.25rem .55rem;font-size:.68rem;font-weight:800;text-transform:capitalize;white-space:nowrap}.status-active{background:#dcfce7;color:#166534}.status-completed{background:#dbeafe;color:#1d4ed8}.status-archived{background:#f3f4f6;color:#4b5563}
.student-workspace{display:grid;grid-template-columns:minmax(0,1.65fr) minmax(19rem,.85fr);gap:1.25rem;align-items:start}.student-search{display:flex;align-items:center;gap:.45rem;margin-top:1rem;border:1px solid #dbe7f4;border-radius:.55rem;padding:.52rem .7rem;color:#4380bd}.student-search input{width:100%;border:0;outline:0;font-size:.75rem}.student-table-wrap{overflow:auto;margin-top:1rem}.student-table{width:100%;min-width:34rem;border-collapse:separate;border-spacing:0}.student-table th{padding:.65rem .5rem;border-bottom:1px solid color-mix(in srgb,var(--color-ink) 10%,transparent);color:#7890a9;font-size:.65rem;text-align:left;text-transform:uppercase}.student-table td{padding:.66rem .5rem;border-bottom:1px solid color-mix(in srgb,var(--color-ink) 8%,transparent);color:#54708d;font-size:.72rem}.student-table tbody tr{cursor:pointer}.student-table tbody tr:hover,.student-row-active{background:#f1f8ff}.student-table td:nth-child(2)>span:last-child{display:inline-block;vertical-align:middle;max-width:12rem}.student-table b,.student-table small{display:block}.student-table b{color:#183451;font-size:.72rem}.student-table small{overflow:hidden;margin-top:.14rem;color:#7b91a7;font-size:.64rem;text-overflow:ellipsis;white-space:nowrap}.student-avatar,.student-profile-avatar{display:inline-grid;width:1.8rem;height:1.8rem;place-items:center;margin-right:.45rem;border-radius:50%;background:linear-gradient(140deg,#d9ecff,#83bdff);color:#0757ae;font-size:.72rem;font-weight:800;vertical-align:middle}.student-table em,.student-detail-head em{display:inline-flex;border-radius:999px;background:#dcfce7;padding:.16rem .4rem;color:#16803d;font-size:.6rem;font-style:normal;font-weight:800}.row-action{color:#436985;font-weight:900}.student-detail{position:sticky;top:1rem}.student-detail-head{display:grid;grid-template-columns:auto 1fr auto;align-items:start;gap:.65rem}.student-profile-avatar{width:3.15rem;height:3.15rem;margin:0;font-size:1.1rem}.student-detail-head h2{margin:.15rem 0;color:#123151;font-size:1.05rem;font-weight:800}.student-detail-head small{display:block;color:#6f8aa4;font-size:.68rem}.student-detail-head em{margin-top:.35rem}.student-facts{display:grid;grid-template-columns:repeat(3,1fr);gap:.35rem;margin:1rem 0;border-top:1px solid color-mix(in srgb,var(--color-ink) 8%,transparent);border-bottom:1px solid color-mix(in srgb,var(--color-ink) 8%,transparent);padding:.75rem 0}.student-facts span{color:#748ca4;font-size:.63rem;text-align:center}.student-facts b{display:block;color:#163757;font-size:.9rem}.detail-section{margin-top:1rem}.enrollment-list{display:grid;gap:.2rem;margin-top:.55rem}.enrollment-list article{display:flex;align-items:center;gap:.55rem;border-bottom:1px solid color-mix(in srgb,var(--color-ink) 8%,transparent);padding:.55rem 0}.enrollment-list b,.enrollment-list small{display:block}.enrollment-list b{font-size:.7rem}.enrollment-list small{margin-top:.12rem;color:#7891a7;font-size:.62rem}.student-data-note{margin-top:1rem;border-radius:.6rem;background:#f4f8fc;padding:.65rem;color:#66809b;font-size:.66rem;line-height:1.45}
.tab-button{display:flex;align-items:center;gap:.65rem}.tab-button span{display:grid;width:1.25rem;place-items:center;color:var(--color-accent)}.stats-grid{display:grid;gap:1rem;grid-template-columns:repeat(3,minmax(0,1fr))}.stat-card{border:1px solid color-mix(in srgb,var(--color-ink) 12%,transparent);border-radius:1rem;background:white;padding:1.15rem;text-align:left;transition:.2s}.stat-card:hover,.course-card:hover{transform:translateY(-2px);border-color:var(--color-accent);box-shadow:0 10px 24px rgb(0 0 0 / 30%)}.stat-card span,.stat-card small,.course-card small,.course-card em{display:block;color:var(--color-muted-ink);font-size:.78rem}.stat-card b{display:block;margin:.3rem 0 .45rem;font-family:serif;font-size:2.4rem}.course-grid{display:grid;gap:1rem;grid-template-columns:repeat(auto-fit,minmax(13rem,1fr))}.course-card{min-height:12rem;border:1px solid color-mix(in srgb,var(--color-ink) 12%,transparent);border-radius:1rem;background:#fff;padding:1rem;text-align:left;transition:.2s}.course-card b{display:block;margin:.7rem 0 .4rem;font-size:1rem}.course-card small{line-height:1.45}.course-card em{margin-top:1rem;color:var(--color-accent);font-style:normal;font-weight:700}.course-price{display:inline-block;border-radius:999px;background:color-mix(in srgb,var(--color-primary) 11%,var(--color-surface));padding:.2rem .5rem;color:var(--color-accent);font-size:.7rem;font-weight:800}
.profile-workspace{display:grid;grid-template-columns:minmax(18rem,.8fr) minmax(0,1.2fr);gap:1.25rem;align-items:start}.profile-preview{background:linear-gradient(145deg,#f6fbff,#e8f4ff)}.profile-identity{display:flex;align-items:center;gap:1rem;margin-top:1rem}.profile-identity img,.profile-identity>span{display:grid;width:4.2rem;height:4.2rem;place-items:center;border-radius:50%;object-fit:cover;background:#bbdcff;color:#0757ae;font-size:1.35rem;font-weight:800}.profile-identity h2{font-size:1.25rem;font-weight:800}.profile-identity p{margin-top:.2rem;color:#2e69a0;font-size:.76rem}.profile-identity small{display:block;margin-top:.4rem;color:#718aa2;font-size:.66rem}.profile-bio{margin-top:1rem;color:#4f6d89;font-size:.78rem;line-height:1.65}.profile-form{display:grid;gap:1rem}.article-workspace{display:grid;grid-template-columns:minmax(18rem,.7fr) minmax(0,1.3fr);gap:1.25rem;align-items:start}.article-list{position:sticky;top:1rem}.article-list-items{display:grid;margin-top:.7rem}.article-list-items button{display:grid;grid-template-columns:auto 1fr auto;align-items:center;gap:.6rem;border-bottom:1px solid color-mix(in srgb,var(--color-ink) 8%,transparent);padding:.7rem 0;text-align:left}.article-list-items b,.article-list-items small{display:block}.article-list-items b{font-size:.74rem}.article-list-items small{margin-top:.15rem;color:#718aa1;font-size:.64rem}.article-list-items em{border-radius:999px;background:#dcfce7;padding:.15rem .4rem;color:#16803d;font-size:.58rem;font-style:normal;font-weight:800}.article-list-items button:hover,.article-selected{background:#f1f8ff}.article-editor{min-height:34rem}
.manage-workspace{max-width:68rem}.manage-workspace>form{border-top:3px solid #1784ff}.manage-workspace>div:last-child{margin-top:1.25rem}.project-management>div:last-child>div,.certificate-management>div:last-child>div{display:grid;grid-template-columns:repeat(auto-fit,minmax(14rem,1fr));gap:.75rem}.project-management .manage-row,.certificate-management .manage-row{min-height:7.5rem;align-items:flex-start;flex-direction:column;border-color:#dfebf6;border-radius:1rem;background:linear-gradient(145deg,#fff,#f8fbff)}.project-management .manage-row span:last-child,.certificate-management .manage-row span:last-child{width:100%;justify-content:flex-end}.experience-management .manage-row{position:relative;border-left:3px solid #1784ff;border-radius:.45rem;background:#fbfdff}.experience-management .manage-row:first-child:before{content:'';position:absolute;top:-1rem;bottom:calc(100% + .1rem);left:-3px;border-left:1px dashed #a5c9ec}
.success {
  background: color-mix(in srgb, var(--color-primary) 18%, var(--color-surface));
  color: var(--color-accent);
}
.failure {
  background: color-mix(in srgb, var(--color-primary) 18%, var(--color-surface));
  color: var(--color-accent);
}

/* Keep the administration surface on the same purple-on-black brand system. */
.admin-sidebar { background: linear-gradient(160deg, var(--color-canvas), color-mix(in srgb, var(--color-canvas) 72%, var(--color-primary))); box-shadow: 0 18px 45px rgb(0 0 0 / 32%); color: var(--color-muted-ink); }
.brand, .tab-button:hover { color: var(--color-ink); }
.tab-button { color: var(--color-muted-ink); }
.tab-button:hover { background: color-mix(in srgb, var(--color-primary) 20%, transparent); }
.tab-active, .primary-button { background: var(--color-primary); color: white; }
.admin-search, .topbar-tools button, .quick-actions, .course-library-actions label, .student-search { border-color: color-mix(in srgb, var(--color-primary) 38%, transparent); background: var(--color-surface); color: var(--color-accent); }
.quick-actions button, .panel-heading button { background: color-mix(in srgb, var(--color-primary) 18%, var(--color-surface)); color: var(--color-accent); }
.stat-card, .course-card { border-color: color-mix(in srgb, var(--color-ink) 14%, transparent); box-shadow: 0 5px 18px rgb(0 0 0 / 18%); }
.stat-card .stat-icon, .avatar-placeholder, .course-thumb, .course-library-thumb { background: color-mix(in srgb, var(--color-secondary) 18%, var(--color-surface)); color: var(--color-accent); }
.compact-list em, .article-list-items em, .student-table em, .student-detail-head em, .status-active, .status-completed, .status-archived { background: color-mix(in srgb, var(--color-secondary) 18%, var(--color-surface)); color: var(--color-accent); }
.course-status-summary span, .student-data-note { background: color-mix(in srgb, var(--color-primary) 14%, var(--color-surface)); color: var(--color-muted-ink); }
.course-status-summary b, .student-table b, .student-detail-head h2, .student-facts b { color: var(--color-ink); }
.workspace-banner { background: linear-gradient(130deg, var(--color-canvas), color-mix(in srgb, var(--color-canvas) 62%, var(--color-primary))); color: var(--color-ink); }
.workspace-banner .eyebrow { color: var(--color-accent); }
.workspace-banner .text-muted-ink { color: var(--color-muted-ink); }
.student-table th, .student-table td, .student-table small, .student-detail-head small, .student-facts span, .enrollment-list small { color: var(--color-muted-ink); }
.student-table tbody tr:hover, .student-row-active, .article-list-items button:hover, .article-selected { background: color-mix(in srgb, var(--color-primary) 14%, var(--color-surface)); }
.student-avatar, .student-profile-avatar { background: linear-gradient(140deg, var(--color-primary), var(--color-secondary)); color: white; }
.row-action, .profile-identity p { color: var(--color-accent); }
.profile-preview { background: linear-gradient(145deg, var(--color-surface), color-mix(in srgb, var(--color-surface) 72%, var(--color-primary))); }
.profile-identity img, .profile-identity > span { background: color-mix(in srgb, var(--color-primary) 24%, var(--color-surface)); color: var(--color-accent); }
.profile-identity small, .profile-bio { color: var(--color-muted-ink); }
.manage-workspace > form { border-top-color: var(--color-primary); }
.project-management .manage-row, .certificate-management .manage-row { border-color: color-mix(in srgb, var(--color-ink) 14%, transparent); background: var(--color-surface); }
.experience-management .manage-row { border-left-color: var(--color-secondary); background: var(--color-surface); }
.experience-management .manage-row:first-child:before { border-left-color: var(--color-secondary); }
.success, .failure { background: color-mix(in srgb, var(--color-primary) 18%, var(--color-surface)); color: var(--color-accent); }
.editor-card, .field, .secondary-button, .course-card, .stat-card { background: var(--color-surface); }
.field, .secondary-button { color: var(--color-ink); }
.editor-card { box-shadow: 0 12px 30px rgb(0 0 0 / 20%); }
.admin-search input, .course-library-actions input, .student-search input { color: var(--color-ink); }
@media (max-width: 1023px) {
  .admin-sidebar {
    min-height: auto;
  }
  .admin-sidebar nav {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
  .admin-sidebar .tab-button {
    text-align: center;
  }
}
@media (max-width: 900px){.course-workspace{grid-template-columns:1fr}.course-library{position:static}}
@media (max-width: 1050px){.student-workspace{grid-template-columns:1fr}.student-detail{position:static}}
@media (max-width: 900px){.profile-workspace,.article-workspace{grid-template-columns:1fr}.article-list{position:static}}
@media (max-width: 900px){.stats-grid{grid-template-columns:repeat(2,minmax(0,1fr))}.dashboard-panels{grid-template-columns:1fr}.dashboard-welcome{flex-direction:column}}
@media (max-width: 700px){.stats-grid{grid-template-columns:1fr}.course-grid{grid-template-columns:1fr}.quick-actions{grid-template-columns:1fr}.quick-actions b{grid-column:auto}}
@media (max-width: 520px) {
  .admin-sidebar nav {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .manage-row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
