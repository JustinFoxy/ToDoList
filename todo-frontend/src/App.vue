<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

// ----------------------
// 1. 登录 & 注册 相关
// ----------------------

// 当前登录的用户（后端返回的 user 对象）
const currentUser = ref(null)

// 登录表单
const loginName = ref('')
const loginPassword = ref('')

// ⭐ 是否显示注册页面
const isRegisterPage = ref(false)

// ⭐ 注册表单
const registerName = ref('')
const registerPassword = ref('')

// localStorage 的 key
const USER_STORAGE_KEY = 'todo_user'

// 从 localStorage 里把用户读出来（页面刷新后用）
const loadUserFromStorage = () => {
  const raw = localStorage.getItem(USER_STORAGE_KEY)
  if (!raw) return
  try {
    currentUser.value = JSON.parse(raw)
  } catch (e) {
    console.error('解析本地用户信息失败', e)
  }
}

// 登录
const login = async () => {
  const name = loginName.value.trim()
  const password = loginPassword.value.trim()
  if (!name || !password) {
    alert('请输入用户名和密码')
    return
  }

  try {
    const res = await axios.post('/api/users/login', { name, password })

    currentUser.value = res.data
    // 存到 localStorage，刷新页面还能记住
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(res.data))

    // 登录之后重新加载一下 todo
    await loadTodos()
  } catch (e) {
    console.error(e)
    alert('登录失败，请检查用户名或密码')
  }
}

// 注册
const register = async () => {
  const name = registerName.value.trim()
  const password = registerPassword.value.trim()

  if (!name || !password) {
    alert('请输入用户名和密码')
    return
  }

  try {
    await axios.post('/api/users/register', { name, password })

    alert('注册成功，请登录 ✨')

    // 清空注册表单
    registerName.value = ''
    registerPassword.value = ''

    // 回到登录页
    isRegisterPage.value = false
  } catch (e) {
    console.error(e)
    alert('注册失败，可能是用户名已存在')
  }
}

// 退出登录
const logout = () => {
  currentUser.value = null
  localStorage.removeItem(USER_STORAGE_KEY)
  todos.value = [] // 清空当前列表

  // 退出后回到“登录页”，并且清空登录输入
  loginName.value = ''
  loginPassword.value = ''
  isRegisterPage.value = false
}

// ----------------------
// 2. Todo 列表相关
// ----------------------

// Todo 列表
const todos = ref([])

// 新任务输入框
const newContent = ref('')

// ➕ 子任务输入框：key = 父任务 id，value = 文本
const childInputs = ref({})

// 只拿“顶级任务”（父任务，parentId 为 null 的）
const rootTodos = computed(() =>
  todos.value.filter((t) => t.parentId == null)
)

// 某个父任务的所有子任务
const childrenOf = (todo) =>
  todos.value.filter((t) => t.parentId === todo.id)

// 当前正在编辑的 todo
const editingId = ref(null)
const editingContent = ref('')

// 加载当前用户的 Todo
const loadTodos = async () => {
  // 没有登录用户：不加载任何 todo，保持空列表
  if (!currentUser.value) {
    todos.value = []
    return
  }

  // 带上 userId 去后端查询
  const res = await axios.get('/api/todos', {
    params: {
      userId: currentUser.value.id
    }
  })
  todos.value = res.data
}

// 新增 Todo（父任务）
const addTodo = async () => {
  const content = newContent.value.trim()
  if (!content) return

  if (!currentUser.value) {
    alert('请先登录，再添加待办事项')
    return
  }

  const res = await axios.post('/api/todos', {
    content,
    userId: currentUser.value.id
  })
  todos.value.push(res.data)
  newContent.value = ''
}

// 新增子任务
const addSubTodo = async (parent) => {
  const text = (childInputs.value[parent.id] || '').trim()
  if (!text) return

  if (!currentUser.value) {
    alert('请先登录，再添加子任务')
    return
  }

  const res = await axios.post('/api/todos', {
    content: text,
    userId: currentUser.value.id,
    parentId: parent.id // ⭐ 关键：把父任务 id 传给后端
  })

  todos.value.push(res.data)
  childInputs.value[parent.id] = '' // 清空当前输入框
}

// 开始编辑
const startEdit = (todo) => {
  editingId.value = todo.id
  editingContent.value = todo.content
}

// 取消编辑
const cancelEdit = () => {
  editingId.value = null
  editingContent.value = ''
}

// 保存编辑（PUT）
const saveEdit = async (todo) => {
  const content = editingContent.value.trim()
  if (!content) return

  const res = await axios.put(`/api/todos/${todo.id}`, {
    ...todo,
    content
  })

  const index = todos.value.findIndex((t) => t.id === todo.id)
  if (index !== -1) {
    todos.value[index] = res.data
  }
  cancelEdit()
}

// 切换完成状态
const toggleCompleted = async (todo) => {
  const res = await axios.put(`/api/todos/${todo.id}`, {
    ...todo,
    completed: todo.completed === 1 ? 0 : 1
  })

  const index = todos.value.findIndex((t) => t.id === todo.id)
  if (index !== -1) {
    todos.value[index] = res.data
  }
}

// 删除 Todo（父任务 or 子任务）
const deleteTodo = async (todo) => {
  await axios.delete(`/api/todos/${todo.id}`)
  todos.value = todos.value.filter((t) => t.id !== todo.id)
}

// 组件挂载时：先读本地登录信息，再加载一次 todo
onMounted(async () => {
  loadUserFromStorage()
  await loadTodos()
})
</script>

<template>
  <div class="page">
    <!-- 如果还没登录：显示 登录 / 注册 页 -->
    <template v-if="!currentUser">
      <!-- 登录页 -->
      <div v-if="!isRegisterPage" class="login-page">
        <div class="login-card">
          <h1 class="login-title">🔐 您好，请登录</h1>

          <div class="login-form">
            <input v-model="loginName" type="text" placeholder="用户名" />
            <input
              v-model="loginPassword"
              type="password"
              placeholder="密码"
            />
            <button @click="login">登录</button>
          </div>

          <p class="login-tip">
            还没有账号？
            <a href="javascript:;" @click="isRegisterPage = true"> 去注册 → </a>
          </p>
        </div>
      </div>

      <!-- 注册页 -->
      <div v-else class="login-page">
        <div class="login-card">
          <h1 class="login-title">🆕 注册</h1>

          <div class="login-form">
            <input v-model="registerName" type="text" placeholder="设置用户名" />
            <input
              v-model="registerPassword"
              type="password"
              placeholder="设置密码"
            />
            <button @click="register">注册</button>
          </div>

          <p class="login-tip">
            已经有账号？
            <a href="javascript:;" @click="isRegisterPage = false">
              返回登录 →
            </a>
          </p>
        </div>
      </div>
    </template>

    <!-- 已登录：ToDo 主界面 -->
    <div v-else class="app">
      <!-- 顶部标题 + 用户信息 -->
      <div class="app-header">
        <h1>📝 Moshe's ToDo List</h1>
        <div class="user-info">
          <span>当前用户：<strong>{{ currentUser.name }}</strong></span>
          <button class="logout" @click="logout">退出登录</button>
        </div>
      </div>

      <!-- 输入框 + 按钮（父任务） -->
      <div class="add-box">
        <input
          v-model="newContent"
          type="text"
          placeholder="写点什么要做的事，比如：写作业、看书、听播客等"
          @keyup.enter="addTodo"
        />
        <button @click="addTodo">添加</button>
      </div>

      <!-- 列表（包一层，让里面自己滚动） -->
      <div class="list-wrapper">
        <ul class="todo-list">
          <!-- 父任务：只遍历 rootTodos -->
          <li
            v-for="todo in rootTodos"
            :key="todo.id"
            :class="{ done: todo.completed === 1 }"
          >
            <!-- ① 父任务本身 -->
            <template v-if="editingId !== todo.id">
              <span class="content" @click="toggleCompleted(todo)">
                {{ todo.content }}
              </span>
              <span class="time">
                {{ todo.createTime?.replace('T', ' ') ?? '' }}
              </span>

              <div class="actions">
                <button @click="startEdit(todo)">编辑</button>
                <button class="danger" @click="deleteTodo(todo)">删除</button>
              </div>
            </template>

            <!-- ② 父任务编辑状态 -->
            <template v-else>
              <input
                v-model="editingContent"
                type="text"
                class="edit-input"
                @keyup.enter="saveEdit(todo)"
              />
              <div class="actions">
                <button @click="saveEdit(todo)">保存</button>
                <button class="danger" @click="cancelEdit">取消</button>
              </div>
            </template>

            <!-- ③ 子任务列表 -->
            <ul class="sub-list">
              <li
                v-for="child in childrenOf(todo)"
                :key="child.id"
                class="sub-item"
                :class="{ done: child.completed === 1 }"
              >
                <!-- 子任务：正常显示状态 -->
                <template v-if="editingId !== child.id">
                  <span class="content" @click="toggleCompleted(child)">
                    {{ child.content }}
                  </span>
                  <span class="time">
                    {{ child.createTime?.replace('T', ' ') ?? '' }}
                  </span>
                  <div class="actions">
                    <button @click="startEdit(child)">编辑</button>
                    <button class="danger" @click="deleteTodo(child)">
                      删除
                    </button>
                  </div>
                </template>

                <!-- 子任务：编辑状态 -->
                <template v-else>
                  <input
                    v-model="editingContent"
                    type="text"
                    class="edit-input"
                    @keyup.enter="saveEdit(child)"
                  />
                  <div class="actions">
                    <button @click="saveEdit(child)">保存</button>
                    <button class="danger" @click="cancelEdit">取消</button>
                  </div>
                </template>
              </li>
            </ul>

            <!-- ④ 添加子任务输入框 -->
            <div class="add-subtask">
              <input
                v-model="childInputs[todo.id]"
                type="text"
                placeholder="添加一个子任务，例如：超慢跑 20 分钟"
                @keyup.enter="addSubTodo(todo)"
              />
              <button @click="addSubTodo(todo)">添加子任务</button>
            </div>
          </li>
        </ul>
      </div>

      <p v-if="todos.length === 0" class="empty">
        现在还没有任务，先添加一个吧～
      </p>
    </div>
  </div>
</template>

<style scoped>
/* 整个页面背景 */
.page {
  height: 100vh; /* 固定一屏 */
  padding: 24px;
  background: #0d1b2a;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ① 登录 / 注册 页样式 */
.login-page {
  width: 100%;
  display: flex;
  justify-content: center;
}

.login-card {
  width: 600px;
  padding: 32px 28px;
  border-radius: 24px;
  background: #0b1725;
  color: #e0e1dd;
  box-shadow: 0 24px 50px rgba(0, 0, 0, 0.6);
  text-align: center;
}

.login-title {
  font-size: 24px;
  margin-bottom: 24px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.login-form input {
  padding: 10px 12px;
  border-radius: 999px;
  border: 1px solid #415a77;
  background: #0d1b2a;
  color: #e0e1dd;
  font-size: 14px;
}

.login-form button {
  margin-top: 4px;
  padding: 10px 16px;
  border-radius: 999px;
  border: none;
  cursor: pointer;
  background: #52b788;
  color: #0b1725;
  font-weight: 600;
  font-size: 15px;
}

.login-tip {
  margin-top: 16px;
  font-size: 12px;
  color: #778da9;
}

.login-tip a {
  color: #52b788;
  cursor: pointer;
  text-decoration: none;
  font-weight: 600;
}

/* ② ToDo 主界面 */
.app {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 28px 32px;
  border-radius: 24px;
  background: #0b1725;
  color: #e0e1dd;
  box-shadow: 0 24px 50px rgba(0, 0, 0, 0.6);
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro Text',
    'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.app-header h1 {
  font-size: 24px;
  margin: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.user-info .logout {
  padding: 4px 12px;
  border-radius: 999px;
  border: none;
  cursor: pointer;
  background: #9e2a2b;
  color: #e0e1dd;
  font-size: 13px;
}

/* 输入框 + 添加按钮 */
.add-box {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.add-box input {
  flex: 1;
  padding: 10px 12px;
  border-radius: 999px;
  border: 2px solid #778da9;
  background: #e0e1dd;
  color: #000000;
  font-size: 14px;
}

.add-box button {
  padding: 10px 16px;
  border-radius: 999px;
  border: none;
  cursor: pointer;
  background: #52b788;
  color: #0b1725;
  font-weight: 600;
}

/* 列表外层：让列表自己滚动 */
.list-wrapper {
  max-height: 55vh;
  overflow-y: auto;
  padding-right: 4px;
  margin-top: 8px;
}

/* 列表 */
.todo-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.todo-list li {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 14px;
  border-radius: 12px;
  background: #0d1b2a;
  border: 1px solid #1b263b;
  margin-bottom: 10px;
}

.todo-list li.done .content {
  text-decoration: line-through;
  color: #6b7280;
}

.content {
  cursor: pointer;
  font-size: 16px;
}

.time {
  font-size: 12px;
  color: #6b7280;
}

.actions {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}

.actions button {
  padding: 4px 12px;
  border-radius: 999px;
  border: none;
  cursor: pointer;
  font-size: 12px;
  background: #415a77;
  color: #e0e1dd;
}

.actions .danger {
  background: #9e2a2b;
  color: white;
}

.edit-input {
  width: 100%;
  padding: 6px 8px;
  border-radius: 8px;
  border: 1px solid #374151;
  background: #020617;
  color: #f9fafb;
}

.empty {
  margin-top: 16px;
  text-align: center;
  color: #6b7280;
}

/* 子任务列表：缩进 + 虚线分隔 */
.sub-list {
  list-style: none;
  padding: 8px 0 0 16px;
  margin: 8px 0 0;
  border-top: 1px dashed #1b263b;
}

/* 子任务单项 */
.sub-item {
  margin-bottom: 6px;
  padding: 8px 10px;
  border-radius: 8px;
  background: #020817;
  border: 1px solid #1b263b;
}

/* 添加子任务区域 */
.add-subtask {
  margin-top: 8px;
  display: flex;
  gap: 6px;
}

.add-subtask input {
  flex: 1;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid #334155;
  background: #020617;
  color: #e5e7eb;
  font-size: 13px;
}

.add-subtask button {
  padding: 6px 10px;
  border-radius: 999px;
  border: none;
  cursor: pointer;
  background: #38bdf8;
  color: #0f172a;
  font-size: 13px;
  font-weight: 600;
}

</style>