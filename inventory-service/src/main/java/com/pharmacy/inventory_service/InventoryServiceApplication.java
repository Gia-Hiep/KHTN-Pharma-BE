package com.pharmacy.inventory_service;

import com.pharmacy.inventory_service.service.InventoryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

	/**
	 * Auto-repair corrupted qtyReserved on startup.
	 * Recalculates from actual ACTIVE reservation items.
	 * Safe to run repeatedly — idempotent.
	 */
	@Bean
	CommandLineRunner repairOnStartup(InventoryService inventoryService) {
		return args -> {
			try {
				var result = inventoryService.repairReservedQuantities();
				System.out.println("✅ [STARTUP] Repaired stock reservations: " + result);
				// Debug: print stock summary for all medicines
				var summaries = inventoryService.summary(null);
				System.out.println("📊 [STARTUP] Stock summary after repair:");
				for (var s : summaries) {
					System.out.printf("   medicineId=%d (%s): onHand=%d, reserved=%d, available=%d%n",
							s.medicineId(), s.medicineName(), s.totalQty(), s.reservedQty(), s.availableQty());
				}
				// Debug: show lots for medicineId=5 specifically
				var lots5 = inventoryService.listLots(5L, null, false);
				System.out.println("🔍 [STARTUP] All lots for medicineId=5:");
				for (var l : lots5) {
					System.out.printf("   lotId=%d, lot=%s, onHand=%d, reserved=%d, avail=%d, expiry=%s%n",
							l.id(), l.lotNumber(), l.qtyOnHand(), l.qtyReserved(), l.available(), l.expiryDate());
				}
			} catch (Exception e) {
				System.err.println("⚠️ [STARTUP] Failed to repair reservations: " + e.getMessage());
			}
		};
	}
}
