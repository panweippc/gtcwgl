# 进销存项目（gtcwgl）Git 使用全流程手册

> 适用对象：零 Git 基础、第一次参与本项目开发的成员。
> 核心心法：**克隆一次、每天先拉后改、改完先提交本地、推送前必须打招呼。**
> 仓库已核对：远程 `git@github.com:panweippc/gtcwgl.git`，主分支 `main`，`.gitignore` 已配好。

---

## 第 0 步：核心概念

| 名词 | 大白话 |
|------|--------|
| **仓库（Repository）** | 放代码 + 记录所有改动的文件夹，本地和 GitHub 上各有一份 |
| **远程（Remote，叫 `origin`）** | GitHub 上那份"总仓库"，名字默认叫 `origin` |
| **分支（Branch）** | 一条独立的开发线，默认叫 `main`（主线） |
| **提交（Commit）** | 给当前改动拍一张"快照"，存到本地，必须写说明 |
| **推送（Push）** | 把本地提交上传到 GitHub 远程仓库 |
| **拉取（Pull）** | 把远程别人改好的代码下载合并到本地 |
| **工作区 / 暂存区 / 版本库** | 你正在改的文件 → `git add` 后的待提交区 → `git commit` 后的历史 |

---

## 第一阶段：一次性环境准备（每台电脑只做一遍）

**1. 安装 Git**
- 官网 https://git-scm.com 下载，一路下一步。装完在文件夹里右键应出现"Git Bash Here"。
- 验证：打开 Git Bash，输入 `git --version`，能看到版本号就成功。

**2. 设置身份（必须，否则提交会报错）**
```bash
git config --global user.name "你的姓名"
git config --global user.email "你的邮箱"
```

**3. 配置 SSH 密钥（本项目仓库是 SSH 地址，必须配）**
```bash
ssh-keygen -t ed25519 -C "你的邮箱"
```
- 一路回车，会在 `C:\Users\你的用户名\.ssh\` 下生成 `id_ed25519.pub`（公钥）。
- 用记事本打开 `id_ed25519.pub`，**复制全部内容**，发给项目负责人（李智鑫），由他把你的公钥加到 GitHub 账户 `panweippc` 里，你才能克隆/推送。
> ⚠️ 私钥 `id_ed25519` 绝不要发给任何人。没加公钥前 `git clone` 会报 `Permission denied (publickey)`。

---

## 第二阶段：首次把代码拿到本地（克隆，只做一次）

**4. 在放代码目录右键 → Git Bash Here，执行：**
```bash
git clone git@github.com:panweippc/gtcwgl.git
cd gtcwgl
```

**5. 确认站在正确分支：**
```bash
git branch                  # 带 * 的是当前分支，应是 main
git remote -v               # 应显示 git@github.com:panweippc/gtcwgl.git
```
> ⚠️ 克隆只需一次，之后用 `git pull` 更新。

**6. 安装依赖（非 Git 步骤，但必须）：**
- 前端：`cd jxc-web && npm install`（生成 `node_modules/`，已被忽略，放心）
- 后端：`jxc-server` 用 Maven 构建，按项目文档操作。

---

## 第三阶段：每天开工的标准动作（重要！）

**7. 开始写代码前，先切回主线并拉取最新：**
```bash
git switch main
git pull origin main
```
> ⚠️ **最易被忽略、也最易出事的一步。** 不先 pull 就改，推送时必冲突。每天上班、每次动手前都先 pull。

**8. 看一眼状态：**
```bash
git status          # 正常应显示 nothing to commit, working tree clean
```

---

## 第四阶段：改完代码 → 提交到本地

**9. 看清楚改了什么：**
```bash
git status          # 红字=未暂存改动
git diff            # 看具体改动行（按 q 退出）
```

**10. 逐文件加入暂存区：**
```bash
git add jxc-web/src/xxx.vue
git add jxc-server/src/main/java/.../XxxController.java
```
> ⚠️ **不要用 `git add .` 一把梭**（`.gitignore` 已挡掉大部分垃圾，但仍可能误带 `.env`、本地配置等）。逐文件 add 最安全。

**11. 提交到本地：**
```bash
git commit -m "修改了登录接口的报错提示"
```
- 好写法：`"修复：请假申请编号在详情页显示不一致"`；差写法：`"fix"`、`"改了点东西"`。

**12. 确认提交：**
```bash
git log --oneline -5
```

---

## 第五阶段：推送（协作纪律，务必看）

**13. 推送前再次拉取防冲突：**
```bash
git pull origin main
```

**14. 推送（需负责人同意后再执行）：**
```bash
git push origin main
```
> ⚠️ **本项目硬性纪律：推 main 前必须告知负责人（李智鑫）并获同意，不要默默推。** 主分支直接推送会影响所有人，且部署机自动拉取后会立即生效。
>
> 推荐做法：推到你自己的分支，让负责人合并（见下一阶段）。

**15. 推送后确认：**
```bash
git status    # 应显示 up to date with 'origin/main'
```

---

## 第六阶段：推荐的分支协作法（比直推 main 安全）

**16. 基于最新 main 开个人分支：**
```bash
git switch main
git pull origin main
git switch -c feature/message-center     # 创建并切换分支
```

**17. 改代码 → `git add` → `git commit`**（同第四阶段）。

**18. 推到你的分支：**
```bash
git push origin feature/message-center
```
- 告诉负责人："我在 `feature/message-center` 做好了消息中心，请合并。"
- 由负责人合并入 `main`，你不用碰 main 的推送。

**19. 合并后清理：**
```bash
git switch main
git pull origin main
git branch -d feature/message-center
```

---

## 第七阶段：常见翻车与急救

**20. 改错文件想撤销（还没 add）：**
```bash
git restore <文件名>          # 恢复到上次提交（改动丢失！）
```

**21. 已 add 想撤回暂存（改动还在）：**
```bash
git restore --staged <文件名>
```

**22. 刚 commit 写错说明 / 漏文件（未 push）：**
```bash
git commit --amend -m "正确说明"
```
> ⚠️ 仅限未 push 的提交。

**23. 拉取 / 合并冲突（CONFLICT）：**
- 文件里 `<<<<<<<` `=======` `>>>>>>>` 标出两边不同。
- 编辑器打开，**人工决定保留哪边**，删掉冲突标记。
- 解决完：`git add <冲突文件>` → `git commit`。
> 拿不准就截图问负责人，别硬点。

**24. 临时切分支但改动没写完：**
```bash
git stash           # 暂存改动
git stash pop       # 回来恢复
```

**25. 误提交大文件 / 密码：**
- 没 push：用第 22 步 amend 或第 21 步撤回。
- 已 push：**立刻告诉负责人**，别自己 `reset`。

---

## 本项目专属红线（必须记牢）

1. **远程固定**：`git@github.com:panweippc/gtcwgl.git`，主分支 `main`，不要建无关远程。
2. **这些文件永不提交**（`.gitignore` 已自动挡，add 时仍要心里有数）：
   - `node_modules/`、`jxc-web/node_modules/`、`jxc-server/target/`
   - `*.jar`、`*.war`、`*.rar`、`*.zip`
   - `tools/`（maven/nginx 等工具二进制）
   - `uploads/`、`jxc-server/uploads/`
   - `.vscode/`、`.trae/`、`.idea/`、`.iml`
   - `.DS_Store`、`Thumbs.db`、`desktop.ini`
3. **推送纪律**：推 main 前必须告知负责人并获同意；推荐个人分支 + 负责人合并。
4. **后端重启不用你管**：改完后端并推送后，部署机后端进程由负责人/自动化处理，你只管代码和提交。
5. **改动超过 3 处或涉及登录逻辑**：先在群里说明改动点，等确认再动手（团队约定）。

---

## 一条龙速查

```bash
git switch main          # 回主线
git pull origin main     # 拉最新
# —— 改代码 ——
git status               # 看改动
git add <具体文件>        # 加文件（别用 .）
git commit -m "<说明>"    # 提交本地
git pull origin main     # 再拉防冲突
# 通知负责人 → 获同意后：
git push origin main     # 推送
```
