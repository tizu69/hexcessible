package dev.tizu.hexcessible;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class HexcessibleClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			var url = "https://g.tizu.dev/hexcessible/@issues";
			client.player.displayClientMessage(
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
		});
	}
}
