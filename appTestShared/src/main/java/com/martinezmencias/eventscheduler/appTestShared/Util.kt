package com.martinezmencias.eventscheduler.appTestShared

import com.martinezmencias.eventscheduler.data.database.EventBasicEntity
import com.martinezmencias.eventscheduler.data.database.EventEntity
import com.martinezmencias.eventscheduler.data.database.EventRoomDataSource
import com.martinezmencias.eventscheduler.data.database.VenueEntity
import com.martinezmencias.eventscheduler.data.datasource.EventLocalDataSource
import com.martinezmencias.eventscheduler.data.datasource.EventRemoteDataSource
import com.martinezmencias.eventscheduler.data.repository.EventRepository
import com.martinezmencias.eventscheduler.data.repository.RegionRepository
import com.martinezmencias.eventscheduler.data.server.*

fun buildEventRepositoryWith(
    localEventBasicData: List<EventBasicEntity>,
    localVenueData: List<VenueEntity>,
    remoteData: List<RemoteEvent>
): EventRepository {
    val locationDataSource = FakeLocationDataSource()
    val permissionChecker = FakePermissionChecker()
    val regionRepository = RegionRepository(locationDataSource, permissionChecker)
    val localDataSource = buildEventRoomDataSource(localEventBasicData, localVenueData)
    val remoteDataSource = buildEventRemoteDataSource(remoteData)
    return EventRepository(regionRepository, localDataSource, remoteDataSource)
}

fun buildRemoteEmbedded(vararg id: String) = RemoteResult(
    embedded = RemoteEmbedded(
        events = buildRemoteEvents(
            *id
        )
    )
)

fun buildRemoteEvents(vararg id: String) = id.map { eventId ->
    RemoteEvent(
        id = eventId,
        name = "Name $eventId",
        images = buildRemoteImages(),
        dates = buildDates(),
        prices = buildRemotePrices(),
        salesUrl = "http://sales.remoteserver.com",
        salesDates = buildSalesDates(),
        embeddedVenues = buildEmbeddedRemoteVenues()
    )
}

fun buildRemoteImages() = listOf(
    RemoteImage(url = "http://images.remoteserver.com/image1.png", ratio = "3_2", width = 640, height = 427),
    RemoteImage(url = "http://images.remoteserver.com/image2.png", ratio = "4_3", width = 305, height = 225),
    RemoteImage(url = "http://images.remoteserver.com/image3.png", ratio = "16_9", width = 640, height = 360),
    RemoteImage(url = "http://images.remoteserver.com/image4.png", ratio = "16_9", width = 2048, height = 1152)
)

fun buildDates() = Dates(Date(time = "2023-05-16T22:00:00Z"))

fun buildRemotePrices() = listOf(
    RemotePrice(type = "standard", min = 1F, max = 2F, currency = "EUR"),
    RemotePrice(type = "standard including fees ", min = 10F, max = 20F, currency = "EUR")
)

fun buildSalesDates() = SalesDates(publicSales = PublicSales(startTime = "2023-05-16T22:00:00Z"))

fun buildEmbeddedRemoteVenues() = RemoteEmbeddedVenues(venues = buildRemoteVenues())

fun buildRemoteVenues() = listOf(
    RemoteVenue(
        id = "ID",
        name = "Name",
        city = City(name = "City"),
        state = State(name = "State"),
        country = Country(name = "Country"),
        address = Address(line1 = "Address")
    )
)

fun buildEventRoomDataSource(
    localEventBasicData: List<EventBasicEntity>,
    localVenueData: List<VenueEntity>,
): EventRoomDataSource {
    val fakeVenueDao = FakeVenueDao(localVenueData)
    val fakeEventDao = FakeEventDao(localEventBasicData, fakeVenueDao)
    return EventRoomDataSource(fakeEventDao, fakeVenueDao)
}

fun buildEventRemoteDataSource(remoteData: List<RemoteEvent>): EventRemoteDataSource {
    val remoteResult = RemoteResult(embedded = RemoteEmbedded(events = remoteData))
    return EventServerDataSource("1234", FakeRemoteService(remoteResult))
}
