package com.example.examplemod.rocket.ship

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

object RocketShipRegistry {

    private val serverShips = ConcurrentHashMap<Long, RocketShipEntity>()
    private val clientShips = ConcurrentHashMap<Long, RocketShipEntity>()
    private val idCounter = AtomicLong(1L)

    @JvmStatic
    fun nextId(): Long = idCounter.getAndIncrement()

    private fun fallbackKey(ship: RocketShipEntity): Long = -ship.id - 1_000_000L

    private fun mapFor(ship: RocketShipEntity): ConcurrentHashMap<Long, RocketShipEntity> =
        if (ship.level().isClientSide) clientShips else serverShips

    @JvmStatic
    fun register(ship: RocketShipEntity) {
        val key = if (ship.shipId >= 0) ship.shipId else fallbackKey(ship)
        mapFor(ship)[key] = ship
    }

    @JvmStatic
    fun remove(shipId: Long) {
        serverShips.remove(shipId)
        clientShips.remove(shipId)
    }

    @JvmStatic
    fun removeByEntity(ship: RocketShipEntity) {
        val map = mapFor(ship)
        if (ship.shipId >= 0) {
            map.remove(ship.shipId)
        } else {
            map.remove(fallbackKey(ship))
        }
    }

    @JvmStatic
    fun get(shipId: Long): RocketShipEntity? = serverShips[shipId] ?: clientShips[shipId]

    @JvmStatic
    fun getAll(): Collection<RocketShipEntity> {
        val all = ArrayList<RocketShipEntity>(serverShips.size + clientShips.size)
        all.addAll(serverShips.values)
        all.addAll(clientShips.values)
        return all
    }

    @JvmStatic
    fun getServerAll(): Collection<RocketShipEntity> = serverShips.values

    @JvmStatic
    fun getClientAll(): Collection<RocketShipEntity> = clientShips.values
}
