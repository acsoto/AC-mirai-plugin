package cc.mcac.mirai.plugin

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.warning
import kotlin.random.Random

/**
 * 使用 kotlin 版请把
 * `src/main/resources/META-INF.services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin`
 * 文件内容改成 `org.example.mirai.plugin.PluginMain` 也就是当前主类全类名
 *
 * 使用 kotlin 可以把 java 源集删除不会对项目有影响
 *
 * 在 `settings.gradle.kts` 里改构建的插件名称、依赖库和插件版本
 *
 * 在该示例下的 [JvmPluginDescription] 修改插件名称，id和版本，etc
 *
 * 可以使用 `src/test/kotlin/RunMirai.kt` 在 ide 里直接调试，
 * 不用复制到 mirai-console-loader 或其他启动器中调试
 */

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "cc.mcac.mirai-plugin",
        name = "AC-mirai-plugin",
        version = "0.1.0"
    ) {
        author("作者名称或联系方式")
        info(
            """
            这是一个测试插件, 
            在这里描述插件的功能和用法等.
        """.trimIndent()
        )
        // author 和 info 可以删除.
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        Config.reload()
        if (Config.host == "") {
            logger.warning { "please fill database information in config.yml" }
        }
        TestCommand.register() // 注册指令
        val eventChannel = GlobalEventChannel.parentScope(this)
        eventChannel.subscribeAlways<GroupMessageEvent> {
            //群消息
            //复读示例
//            if (message.contentToString().startsWith("复读")) {
//                group.sendMessage(message.contentToString().replace("复读", ""))
//            }
            if (message.contentToString() == "#ls") {
                group.sendMessage(SQLManager.getInstance().playerList)
                return@subscribeAlways
            }
            if (message.contentToString().startsWith("#medal ")) {
                val playerName = message.contentToString().replace("#medal ", "")
                val msg = "玩家" + playerName + "的勋章: " + SQLManager.getInstance().getPlayerMedals(playerName)
                if (msg.isEmpty()) {
                    group.sendMessage("该玩家无任何勋章")
                } else {
                    group.sendMessage(msg)
                }
            }
            if (message.contentToString().startsWith("#抽奖 ")) {
                if (hasCratePermission(sender)) {
                    val content = message.contentToString().replace("#抽奖 ", "")
                    val args = content.split(" ")
                    val prize = args[0]
                    val random = (args.indices - 1).random() + 1
                    group.sendMessage("恭喜 " + args[random] + " 获得 " + prize)
                }
            }
            //分类示例
//            message.forEach {
//                //循环每个元素在消息里
//                if (it is Image) {
//                    //如果消息这一部分是图片
//                    val url = it.queryUrl()
//                    group.sendMessage("图片，下载地址$url")
//                }
//                if (it is PlainText) {
//                    //如果消息这一部分是纯文本
//                    group.sendMessage("纯文本，内容:${it.content}")
//                }
//            }
        }
        eventChannel.subscribeAlways<FriendMessageEvent> {
            //好友信息
//            sender.sendMessage("hi")
        }
        eventChannel.subscribeAlways<NewFriendRequestEvent> {
            //自动同意好友申请
//            accept()
        }
        eventChannel.subscribeAlways<BotInvitedJoinGroupRequestEvent> {
            //自动同意加群申请
//            accept()
        }

        myCustomPermission // 注册权限
    }

    // region console 权限系统示例
    private val myCustomPermission by lazy { // Lazy: Lazy 是必须的, console 不允许提前访问权限系统
        // 注册一条权限节点 org.example.mirai-example:my-permission
        // 并以 org.example.mirai-example:* 为父节点

        // @param: parent: 父权限
        //                 在 Console 内置权限系统中, 如果某人拥有父权限
        //                 那么意味着此人也拥有该权限 (org.example.mirai-example:my-permission)
        // @func: PermissionIdNamespace.permissionId: 根据插件 id 确定一条权限 id
        PermissionService.INSTANCE.register(permissionId("my-permission"), "一条自定义权限", parentPermission)
    }

    private val cratePermission by lazy {
        PermissionService.INSTANCE.register(permissionId("crate"), "抽奖权限", parentPermission)
    }

    public fun hasCustomPermission(sender: User): Boolean {
        return when (sender) {
            is Member -> AbstractPermitteeId.ExactMember(sender.group.id, sender.id)
            else -> AbstractPermitteeId.ExactUser(sender.id)
        }.hasPermission(myCustomPermission)
    }

    public fun hasCratePermission(sender: User): Boolean {
        return when (sender) {
            is Member -> AbstractPermitteeId.ExactMember(sender.group.id, sender.id)
            else -> AbstractPermitteeId.ExactUser(sender.id)
        }.hasPermission(cratePermission)
    }

    object Config : ReadOnlyPluginConfig("config") {
        val host: String by value()
        val username_medal: String by value()
        val username_info: String by value()
        val password_medal: String by value()
        val password_info: String by value()
    }


    // endregion
}

object TestCommand : SimpleCommand(
    PluginMain, "foo",
    description = "测试指令"
) {
    // 会自动创建一个 ID 为 "org.example.example-plugin:command.foo" 的权限.


    // 通过 /foo 调用, 参数自动解析
    @Handler
    suspend fun CommandSender.handle() { // 函数名随意, 但参数需要按顺序放置.
        PluginMain.logger.info { SQLManager.getInstance().playerList }
    }
}