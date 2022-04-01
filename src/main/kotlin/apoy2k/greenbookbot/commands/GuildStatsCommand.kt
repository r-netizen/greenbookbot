package apoy2k.greenbookbot.commands

import apoy2k.greenbookbot.await
import apoy2k.greenbookbot.model.Storage
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands

val GuildStatsCommand = Commands.slash("serverstats", "Display server-wide fav stats")

suspend fun executeGuildStats(storage: Storage, event: SlashCommandInteractionEvent) {
    val interaction = event.reply("Fetching favs...").await()

    val favs = storage.getFavs(null, event.guild?.id, emptyList())
    interaction.editOriginal("Found ${favs.size} favs, calculating stats...").await()

    val embed = EmbedBuilder()
        .setTitle("Server Stats")
        .writeStats(favs, event.jda)

    interaction.editOriginal("Got em!").await()
    interaction.editOriginalEmbeds(embed.build()).await()
}
