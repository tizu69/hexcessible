package dev.tizu.hexcessible;

import dev.tizu.hexcessible.smartsig.HexThingsIntrojection;
import dev.tizu.hexcessible.smartsig.HexThingsPatience;
import dev.tizu.hexcessible.smartsig.HexicalMacro;
import dev.tizu.hexcessible.smartsig.OverevalGeb;
import dev.tizu.hexcessible.smartsig.OverevalNephthys;
import dev.tizu.hexcessible.smartsig.OverevalNut;
import dev.tizu.hexcessible.smartsig.OverevalSekhmet;
import dev.tizu.hexcessible.smartsig.SmartSig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class HexcessibleClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		SmartSig.SmartSigRegistry.register(new OverevalNephthys());
		SmartSig.SmartSigRegistry.register(new OverevalGeb());
		SmartSig.SmartSigRegistry.register(new OverevalNut());
		SmartSig.SmartSigRegistry.register(new OverevalSekhmet());
		SmartSig.SmartSigRegistry.register(new HexicalMacro());
		SmartSig.SmartSigRegistry.register(new HexThingsPatience());
		SmartSig.SmartSigRegistry.register(new HexThingsIntrojection());
		
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
