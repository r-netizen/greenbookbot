package apoy2k.greenbookbot.commands

import apoy2k.greenbookbot.await
import apoy2k.greenbookbot.forMessage
import apoy2k.greenbookbot.model.Storage
import apoy2k.greenbookbot.replyError
import apoy2k.greenbookbot.weightedRandom
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

private const val SELF = "self"

val MysteryFavCommand =
    Commands.slash("mystery", "Post a random fav (even of other users), without revealing the author")
        .addOption(OptionType.BOOLEAN, SELF, "Only choose mystery fav from your own favs")

suspend fun executeMysteryFavCommand(storage: Storage, event: SlashCommandInteractionEvent) {
    val guildIds = event.jda.guilds.map { it.id }

    val userId = when (event.getOption(SELF)?.asBoolean) {
        true -> event.user.id
        else -> null
    }

    val interaction = event.reply("Fetching candidate...").await()

    val candidates = storage
        .getFavs(userId, event.guild?.id, emptyList())
        .filter { guildIds.contains(it.guildId) }

    val fav = candidates.weightedRandom()
        ?: return interaction.replyError("No favs found")

    val guild = event.jda.guilds
        .firstOrNull { it.id == fav.guildId }
        ?: return interaction.replyError("Guild not found:\n${fav.guildUrl()}", fav.id)
    val channel = guild.getTextChannelById(fav.channelId)
        ?: guild.getThreadChannelById(fav.channelId)
        ?: return interaction.replyError("Channel not found:\n${fav.channelUrl()}", fav.id)

    val message = retrieveMessageWithErrorHandling(fav, storage, interaction, channel) ?: return
    val embed = EmbedBuilder().forMessage(message, fav.id)
        .setAuthor("Mystery Fav", message.jumpUrl)
        .build()

    interaction.editOriginal(getFavMessage()).await()
    interaction.editOriginalEmbeds(embed).await()

    val original = interaction.retrieveOriginal().await()
    original.addReaction("👍").await()
    original.addReaction("👎").await()
}
