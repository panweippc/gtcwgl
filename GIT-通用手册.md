# Git 全流程通用手册（零基础版）

> 适用对象：第一次用 Git、没有版本控制基础的开发成员。
> 本手册不绑定任何具体项目，所有 `<...>` 处请替换成你所在项目的实际值。
> 核心心法：**克隆一次、每天先拉后改、改完先提交本地、推送前必须打招呼。**

---

## 第 0 步：先搞懂这几个词

| 名词 | 大白话 |
|------|--------|
| **仓库（Repository）** | 存放代码并完整记录所有改动历史的文件夹，本地和远程各有一份 |
| **远程（Remote，默认叫 `origin`）** | 服务器上那份"总仓库"，比如 GitHub / GitLab / Gitee 上的仓库 |
| **分支（Branch）** | 一条独立的开发线，默认主线叫 `main` 或 `master`。建议每人一条自己的线 |
| **提交（Commit）** | 给当前改动拍一张"快照"存进本地历史，必须写说明 |
| **推送（Push）** | 把本地提交上传到远程仓库 |
| **拉取（Pull）** | 把远程别人改好的代码下载并合并到本地 |
| **工作区 / 暂存区 / 版本库** | 你正在改的文件 → `git add` 后的待提交区 → `git commit` 后的历史 |

---

## 第一阶段：环境准备（每台电脑只做一次）

**1. 安装 Git**
- 下载：https://git-scm.com ，一路下一步。装完在文件夹里右键出现"Git Bash Here"即成功。
- 验证：打开 Git Bash，输入 `git --version`，能看到版本号即可。

**2. 设置身份（必须，否则提交会报错）**
```bash
git config --global user.name "<你的姓名>"
git config --global user.email "<你的邮箱>"
```
> 这只用于标记"谁提交的"，用真实姓名方便团队追溯。

**3. 连接远程仓库的两种方式（二选一）**

方式 A：SSH（推荐，配一次后免密码）
```bash
ssh-keygen -t ed25519 -C "<你的邮箱>"      # 一路回车
```
- 生成的公钥在 `C:\Users\你的用户名\.ssh\id_ed25519.pub`（Linux/Mac 在 `~/.ssh/id_ed25519.pub`）。
- 用记事本打开，**复制全部内容**，发给项目负责人，由他把你的公钥加到远程仓库账户里。
- 验证：`ssh -T git@github.com`（github 示例，按你实际平台改）。

方式 B：HTTPS（每次推送输账号密码/令牌）
```bash
git config --global credential.helper store     # 记住凭据，避免反复输入
```
> 注意：很多平台（如 GitHub）已不支持账号密码，需用 **Personal Access Token（令牌）** 当密码。令牌只在创建时显示一次，妥善保存。

> ⚠️ 公钥（`*.pub`）可以公开给管理员；私钥（`id_ed25519`，无后缀）**绝不能外传**。

---

## 第二阶段：首次获取代码（克隆，只做一次）

**4. 在放代码的目录右键 → Git Bash Here，执行：**
```bash
git clone <你的仓库地址>
cd <项目目录>                 # 进入克隆下来的文件夹
```
- 仓库地址形如：
  - SSH：`git@github.com:组织名/项目名.git`
  - HTTPS：`https://github.com/组织名/项目名.git`
- 验证：
```bash
git branch                  # 看本地分支，带 * 的是当前分支（通常是 main）
git remote -v               # 应显示你的远程地址
```

> ⚠️ 克隆只需一次。之后永远用 `git pull` 更新，不要重复 clone。

**5. 安装项目依赖（非 Git 步骤，但必须做才能跑起来）**
- 按项目文档安装（如前端 `npm install`、后端 Maven/Gradle 构建等）。
- 依赖目录一般已被 `.gitignore` 忽略，放心安装。

---

## 第三阶段：每天开工的标准动作（最重要！）

**6. 开始写代码前，先回到主线并拉取最新代码：**
```bash
git switch main             # 切回主线（若你的主线叫 master，改成 git switch master）
git pull origin main        # 把队友推上来的最新代码拉下来合并
```
> ⚠️ **新手最容易忘、也最容易出事的一步。** 不先 pull 就改，推送时几乎必冲突。每天上班、每次准备动手前都先 pull 一次。

**7. 看一眼状态确认干净：**
```bash
git status
```
- 正常应显示 `nothing to commit, working tree clean`。
- 若有陌生文件，先看是不是被忽略的垃圾（见第七阶段红线）。

---

## 第四阶段：改完代码 → 提交到本地

**8. 看清自己改了什么：**
```bash
git status          # 列出改动文件（红字=未暂存）
git diff            # 看具体改了哪些行（按 q 退出）
```

**9. 把要提交的文件加入暂存区（逐文件最安全）：**
```bash
git add <路径/文件名>
git add <另一个文件>
```
> ⚠️ **不要用 `git add .` 一把梭**。虽然 `.gitignore` 挡掉大部分垃圾，但仍可能误带 `.env`、本地配置等敏感/私人文件。逐文件 add 最稳妥。

**10. 提交到本地版本库：**
```bash
git commit -m "<一句话说明这次干了啥>"
```
- 好的写法：`"修复：登录失败时的报错提示不准确"`、`"新增：消息中心列表接口"`
- 差的写法：`"fix"`、`"改了点东西"`（以后翻历史会骂人）

**11. 提交后确认：**
```bash
git log --oneline -5        # 看最近 5 条提交
```

---

## 第五阶段：推送（通用协作纪律）

**12. 推送前，再次拉取防冲突：**
```bash
git pull origin main
```
- 若提示冲突，跳到第六阶段急救处理。

**13. 推送（需团队负责人同意后再做）：**
```bash
git push origin main
```
> ⚠️ **通用纪律：推主线前必须告知负责人并获同意，不要默默推上去。** 主分支直接推送会影响所有人，且可能触发自动部署。
>
> 更稳妥的做法（强烈推荐）：推到你自己的分支，由负责人通过 **Pull Request / Merge Request（PR/MR）** 合并。见下一阶段。

**14. 推送后确认：**
```bash
git status       # 应显示 "up to date with 'origin/main'"
```

---

## 第六阶段：分支协作法（多人开发推荐）

假设你要做"消息中心"功能：

**15. 基于最新主线开一条个人分支：**
```bash
git switch main
git pull origin main
git switch -c feature/message-center     # 创建并切换到新分支
```
> 命名建议：`feature/功能名`、`fix/问题描述`、`hotfix/紧急修复`。

**16. 正常改代码 → `git add` → `git commit`**（同第四阶段）。

**17. 推送到远程的你的分支：**
```bash
git push origin feature/message-center
```
- 然后告诉负责人："我在 `feature/message-center` 做好了消息中心，请合并。"
- 负责人在平台发 PR/MR 或直接本地 `git merge` 合入主线，你不必碰主线的推送。

**18. 合并完成后清理本地分支：**
```bash
git switch main
git pull origin main
git branch -d feature/message-center     # 删除已合并的本地分支
```

---

## 第七阶段：常见翻车与急救（新手必看）

**19. 改错文件，想撤销工作区修改（还没 add）：**
```bash
git restore <文件名>          # 恢复到上次提交状态（未提交改动会丢失！）
```
> ⚠️ 会丢失未提交改动，确认不要了再用。

**20. 已 `git add`，想撤回暂存（改动还在）：**
```bash
git restore --staged <文件名>
```

**21. 刚 `commit` 但写错说明 / 漏了文件（还没 push）：**
```bash
git commit --amend -m "<正确说明>"     # 修改最近一次提交
```
> ⚠️ 仅限"未 push"的提交。已 push 的不要 amend，否则与远程冲突。

**22. 拉取 / 合并出现冲突（CONFLICT）：**
- Git 在冲突文件里用 `<<<<<<<` `=======` `>>>>>>>` 标出两边不同。
- 用编辑器打开，**人工决定保留哪边**（或两边都要），删掉冲突标记符号。
- 解决完执行：
```bash
git add <冲突文件>
git commit            # 完成合并提交
```
> 冲突不可怕，本质是"两人改了同一处，Git 让你拍板"。拿不准就截图问负责人，别硬点。

**23. 临时切分支但手头改动没写完：**
```bash
git stash           # 暂存当前改动（工作区变干净）
# 切去做别的事……
git stash pop       # 回来后恢复改动
```

**24. 误提交了大文件 / 密码 / 密钥：**
- 还没 push：用第 21 步 `amend` 或第 20 步撤回。
- 已 push：**立刻告诉负责人**，不要自己瞎 `reset`/`push -f`，由负责人处理历史重写。

**25. 想回退到某次历史提交（谨慎）：**
```bash
git log --oneline                 # 找到目标提交的哈希（前 7 位即可）
git revert <提交哈希>             # 生成一次"反向提交"抵消改动（安全，不改历史）
```
> ⚠️ 新手优先用 `revert`（不改历史、可push），不要乱用 `reset --hard`（会丢工作）。

---

## 通用红线（任何项目都适用）

1. **永不提交的内容：**
   - 密钥 / 令牌 / 密码文件（`.env`、`*.pem`、`id_rsa` 等）
   - 依赖目录（`node_modules/`、`vendor/`、`venv/`）
   - 编译/构建产物（`dist/`、`build/`、`target/`、`*.class`、`*.jar`、`*.war`）
   - IDE / 编辑器配置（`.vscode/`、`.idea/`、`.iml`）
   - 系统垃圾（`.DS_Store`、`Thumbs.db`、`desktop.ini`）
   - 运行时生成的数据（上传文件、日志、`*.log`）

2. **提交信息要写人话**，说明"为什么改"，不是"改了什么"（代码 diff 自己会说话）。

3. **永远先 `pull` 再 `push`**，减少冲突。

4. **不要对主线强推（`git push -f`）**，会覆盖别人劳动成果。

5. **不确定就问负责人**，别拿生产仓库练手。

### 通用 .gitignore 参考模板
把下面内容放进项目根目录的 `.gitignore`（按需删减）：
```gitignore
# 依赖
node_modules/
vendor/
venv/

# 构建产物
dist/
build/
target/
*.class
*.jar
*.war

# 环境变量 / 密钥
.env
*.pem
id_rsa*

# IDE / 编辑器
.vscode/
.idea/
*.iml

# 系统文件
.DS_Store
Thumbs.db
desktop.ini

# 日志 / 运行时
*.log
uploads/
```

---

## 一条龙速查（日常最常用）

```bash
git switch main              # 回到主线
git pull origin main         # 拉最新
# —— 改代码 ——
git status                   # 看改动
git add <具体文件>            # 加文件（别用 .）
git commit -m "<说明>"        # 提交本地
git pull origin main         # 再拉一次防冲突
# 通知负责人 → 获同意后：
git push origin main         # 推送
```

---

## 常见报错速查

| 报错信息 | 原因 / 解决 |
|----------|------------|
| `Permission denied (publickey)` | SSH 公钥没加到远程账户，回到第一阶段第 3 步 |
| `fatal: not a git repository` | 当前目录不是仓库，先 `cd` 进项目目录 |
| `error: failed to push` / `rejected` | 远程有你本地没有的提交，先 `git pull` 再 push |
| `Please tell me who you are` | 没设身份，执行第一阶段第 2 步 |
| `CONFLICT (content)` | 冲突了，见第七阶段第 22 步 |
| `detached HEAD` | 处于游离状态，执行 `git switch main` 回到主线 |
