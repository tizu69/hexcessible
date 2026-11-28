package dev.tizu.hexcessible;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(value = Hexcessible.MOD_ID, dist = {Dist.CLIENT})
public class HexcessibleClient {
	public HexcessibleClient(IEventBus modEventBus, ModContainer container) {
		NeoForge.EVENT_BUS.addListener(HexcessibleClient::onPlayerJoin);
	}

	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		var url = "https://g.tizu.dev/hexcessible/@issues";
		event.getEntity().displayClientMessage(
				Component.empty()
						.append(
								Component.literal("[!] ").setStyle(
										Style.EMPTY.withColor(ChatFormatting.RED)
								)
						)
						.append("This is an experimental build of ")
						.append(
								Component.literal("Hexcessible").setStyle(
										Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE)
								)
						)
						.append(". Please report any issues you encounter ")
						.append(
								Component.literal("here").setStyle(
										Style.EMPTY.withColor(ChatFormatting.BLUE)
												.withUnderlined(true)
												.withClickEvent(
														new ClickEvent(
																ClickEvent.Action.OPEN_URL,
																url
														)
												)
								)
						)
						.append(Component.literal(".")),
				false
		);
	}
}
