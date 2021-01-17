package ch.dempsey.bedrijfsbeheerder.commands;

import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ch.dempsey.bedrijfsbeheerder.Main;
import ch.dempsey.bedrijfsbeheerder.util.ResponseGeneralisation;
import ch.dempsey.bedrijven.data.util.BAPI;
import ch.dempsey.bedrijven.data.util.BedrijfsType;
import ch.dempsey.bedrijven.data.util.Factuur;
import ch.dempsey.bedrijven.data.util.Werknemer;
import ch.dempsey.mt.api.MojangAPI;
import net.md_5.bungee.api.ChatColor;

public class Bedrijf implements CommandExecutor{

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		/*
		 * /bedrijf 
		 *     info <naam> -- No Permissions --
		 *     		-- Ondernemingsnr: -- MTB -- Eigenaar
		 *     		-- Huidige Eigenaars:
		 *     		-- Naam:
		 *     pin <amnt> <naam> -- MTB Permissions -- Administrator --
		 *     		-- Stort vanop je bedrijfsrekening op je persoonlijke rekening
		 *     stort <amnt> <naam> -- MTB permissions -- Administrator --
		 *          -- Stort vanop je persoonlijke rekening op je bedrijfsrekening
		 * 	   balance <naam>: -- MTB Permissions -- Administrator --
		 * 			-- Laat de balance van je bedrijf zien
		 * 	   
		 * 		werknemers <naam>: -- MTB Permissions -- Team leider --
		 *			-- Laat een lijst met werknemers zien van je bedrijf!
		 *	   		werknemer loon <bedrijf> <werknemer> <nieuwLoon>: -- Administrator
		 *				-- Pas het loon aan van je werknemer
		 *	  	 	werknemer hire <bedrijf> <werknemer> <loon>: -- Administrator
		 *				-- Huur een nieuwe werknemer in!
		 *	   		werknemer fire <bedrijf> <werknemer>: -- Administrator
		 *				-- Onstla je snode werknemer!
		 * 		
		 * 		factuur:
		 * 			create <verkoper> <koper> <bedrijf> <prijs> <beschrijving>:
		 * 				-- Maak een nieuwe factuur aan voor een speler.   	
		 * 			list <bedrijf>: -- MTB -- Werknemer
		 * 				-- Laat een lijst van facturen zien
		 * 			listplayer <naam>: -- MTB -- Werknemer
		 * 				-- Laat een lijst van facturen zien voor een speler!
		 * 
		 * 		start <naam> <startSom> -- everyone
		 * 			-- Start een nieuw bedrijf, als die nog niet bestaat!
		 * */
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(ResponseGeneralisation.NO_PERMISSION);
		} else {
			Player p = (Player)sender;
			
			if(cmd.getName().equalsIgnoreCase("bedrijf")) {
				
				if(args.length == 2) {
					// Info, balance & werknemers
					if(args[0].equalsIgnoreCase("info")) {
						if(BAPI.getBedrijf(args[1]) != null) {
							ch.dempsey.bedrijven.data.util.Bedrijf b = BAPI.getBedrijf(args[1]);
							p.sendMessage(ChatColor.GREEN + "Huidige Eigenaar: " + ChatColor.GRAY + MojangAPI.getName(b.getOwner().toString()));
							p.sendMessage(ChatColor.GREEN + "Naam: " + ChatColor.GRAY + b.getName());
						}else {
							p.sendMessage(ResponseGeneralisation.INVALID_COMPANY);
						}
					}else if(args[0].equalsIgnoreCase("balance")) {
						if(BAPI.getBedrijf(args[1]) != null) {
							ch.dempsey.bedrijven.data.util.Bedrijf b = BAPI.getBedrijf(args[1]);
							if(p.getUniqueId().toString().equals(b.getOwner().toString()) || p.hasPermission("bedrijfs.manager."+b.getId())) {
								p.sendMessage(ChatColor.GREEN + "Bedrijfs Balance: " + ChatColor.GRAY + String.valueOf(b.getBalance()));
							}else {
								p.sendMessage(ResponseGeneralisation.NO_PERMISSION);
							}
						} else {
							p.sendMessage(ResponseGeneralisation.INVALID_COMPANY);
						}
					}else if(args[0].equalsIgnoreCase("werknemers")){
						if(BAPI.getBedrijf(args[1]) != null) {
							ch.dempsey.bedrijven.data.util.Bedrijf b = BAPI.getBedrijf(args[1]);
							if(p.getUniqueId().toString().equals(b.getOwner().toString()) || p.hasPermission("bedrijfs.manager."+b.getId())) {
								p.sendMessage(ChatColor.GREEN + "Werknemers voor " + ChatColor.GRAY + b.getName() + ChatColor.GREEN + ":");
								for(Werknemer w : b.getWerknemers()) {
									p.sendMessage(ChatColor.GRAY + " - " + MojangAPI.getName(w.getUuid().toString()) + " [ref: " + String.valueOf(w.getId()) + "]");
								}
							}else {
								p.sendMessage(ResponseGeneralisation.NO_PERMISSION);
							}
						} else {
							p.sendMessage(ResponseGeneralisation.INVALID_COMPANY);
						}
					}else {
						p.sendMessage(ResponseGeneralisation.INVALID_SUBCOMMAND);
					}
					
				}else if(args.length == 3) {
					// Pin, Stort, start
					// Vault
					if(args[0].equalsIgnoreCase("pin")) {
						if(BAPI.getBedrijf(args[1]) != null) {
							try {
								int amnt = Integer.parseInt(args[2]);
								ch.dempsey.bedrijven.data.util.Bedrijf b = BAPI.getBedrijf(args[1]);
								double current = b.getBalance();
								if(b.getBalance() >= amnt) {
									if(p.getUniqueId().toString().equals(b.getOwner().toString()) || p.hasPermission("bedrijfs.manager."+b.getId())) {
										double rs = current - amnt;
										b.setBalance(rs);
										Main.eco.depositPlayer(p, amnt);
										p.sendMessage(ChatColor.GREEN + "Je hebt €" + ChatColor.GRAY + String.valueOf(amnt) + ChatColor.GREEN + " opgenomen van de rekening van " + ChatColor.GRAY + b.getName() + ChatColor.GREEN + " nieuw totaal: " + ChatColor.GRAY + String.valueOf(b.getBalance()));
									}else {
										p.sendMessage(ResponseGeneralisation.NO_PERMISSION);
									}
								} else {
									p.sendMessage(ResponseGeneralisation.INSUFFICIENT_FUNDS);
								}
								
							}catch(Exception e) {
								p.sendMessage(ResponseGeneralisation.INVALID_AMOUNT);
							}
						} else {
							p.sendMessage(ResponseGeneralisation.INVALID_COMPANY);
						}
					}else if(args[0].equalsIgnoreCase("stort")) {
						if(BAPI.getBedrijf(args[1]) != null) {
							try {
								int amnt = Integer.parseInt(args[2]);
								ch.dempsey.bedrijven.data.util.Bedrijf b = BAPI.getBedrijf(args[1]);
								double current = b.getBalance();
								double rs = current + amnt;
								if(Main.eco.getBalance(p) >= amnt) {
									if(p.getUniqueId().toString().equals(b.getOwner().toString()) || p.hasPermission("bedrijfs.manager."+b.getId())) {
										Main.eco.withdrawPlayer(p, amnt);
										b.setBalance(rs);
										p.sendMessage(ChatColor.GREEN + "Je hebt €" + ChatColor.GRAY + String.valueOf(amnt) + ChatColor.GREEN + " gestort op de rekening van " + ChatColor.GRAY + b.getName() + ChatColor.GREEN + " nieuw totaal: " + ChatColor.GRAY + String.valueOf(b.getBalance()));
									}else {
										p.sendMessage(ResponseGeneralisation.NO_PERMISSION);
									}
								} else {
									p.sendMessage(ResponseGeneralisation.INSUFFICIENT_FUNDS);
								}
								
							}catch(Exception e) {
								p.sendMessage(ResponseGeneralisation.INVALID_AMOUNT);
							}
							
						}else {
							p.sendMessage(ResponseGeneralisation.INVALID_COMPANY);
						}
						
						
					}else if(args[0].equalsIgnoreCase("start")) {
						if(BAPI.getBedrijf(args[1]) == null) {
							try {
								double bal = Double.parseDouble(args[2]);
								if(Main.eco.getBalance(p) >= bal) {
									Main.eco.withdrawPlayer(p, bal);
									BAPI.createBedrijf(p, args[1], bal, BedrijfsType.HANDEL);
									p.sendMessage(ChatColor.GREEN + "Je hebt je bedrijf "+args[1]+" opgericht!");
								} else {
									p.sendMessage(ResponseGeneralisation.INSUFFICIENT_FUNDS);
								}
							}catch(Exception e) {
								p.sendMessage(ResponseGeneralisation.INVALID_AMOUNT);
							}
						}else {
							p.sendMessage(ResponseGeneralisation.COMPANY_EXISTS);
						}
					}else {
						p.sendMessage(ResponseGeneralisation.INVALID_SUBCOMMAND);
					}
					
					
				}else if(args.length == 4) {
					// werknemer fire, factuurListByCompany, FacuurListByPlayer
					if(args[0].equalsIgnoreCase("werknemer") && args[1].equalsIgnoreCase("fire")) {
						if(BAPI.getBedrijf(args[2]) != null) {
							ch.dempsey.bedrijven.data.util.Bedrijf b = BAPI.getBedrijf(args[2]);
							UUID u = UUID.fromString(MojangAPI.getUuid(args[3]));
							b.fireWerknemer(u);
							p.sendMessage(ChatColor.RED + "Je hebt " + args[3] + " ontslaan bij " + b.getName());
							if(p.getUniqueId().toString().equalsIgnoreCase(b.getOwner().toString()) || p.hasPermission("bedrijfs.manager."+b.getId())) {
								
							} else {
								p.sendMessage(ResponseGeneralisation.NO_PERMISSION);
							}
						}else {
							p.sendMessage(ResponseGeneralisation.INVALID_COMPANY);
						}
					}else if(args[0].equalsIgnoreCase("factuur") && args[1].equalsIgnoreCase("list")) {
						if(BAPI.getBedrijf(args[2]) != null) {
							ch.dempsey.bedrijven.data.util.Bedrijf b = BAPI.getBedrijf(args[2]);
							if(p.getUniqueId().toString().equalsIgnoreCase(b.getOwner().toString()) || p.hasPermission("bedrijfs.manager."+b.getId())) {
								p.sendMessage(ChatColor.GREEN + "Facturen voor: " + ChatColor.GRAY + b.getName());
								for(Factuur f : b.getFacturen()) {
									if(f.isPaid()) {
										p.sendMessage(ChatColor.GRAY + " - " + ChatColor.GREEN + MojangAPI.getName(f.getCreator().toString()) + " : " + MojangAPI.getName(f.getClient().toString()) + " : €" + String.valueOf(f.getPrice()));
									} else {
										p.sendMessage(ChatColor.GRAY + " - " + ChatColor.RED + MojangAPI.getName(f.getCreator().toString()) + " : " + MojangAPI.getName(f.getClient().toString()) + " : €" + String.valueOf(f.getPrice()));
									}
								}
							} else {
								p.sendMessage(ResponseGeneralisation.NO_PERMISSION);
							}
							
						}else {
							p.sendMessage(ResponseGeneralisation.INVALID_COMPANY);
						}
					}else if(args[0].equalsIgnoreCase("factuur") && args[1].equalsIgnoreCase("listplayer")) {
						// Not Added In API, TODO
					}else {
						p.sendMessage(ResponseGeneralisation.INVALID_SUBCOMMAND);
					}
				}else if(args.length == 5) {
					// werknemer hire, werknemer loon
					
				}else if(args.length <= 7 ) {
					// factuur create
					
				}else {
					// Send Documentation
				}
				
			}
		}
		
		
		return true;
	}
	
}
