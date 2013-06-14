package denominator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.tryFind;

import java.util.List;

import javax.inject.Inject;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import denominator.model.ResourceRecordSet;
import denominator.model.Zone;
import denominator.model.Zones;
import denominator.model.profile.Geo;
import denominator.profile.GeoResourceRecordSetApi;
import denominator.profile.WeightedResourceRecordSetApi;

/**
 * allows you to manipulate resources such as DNS Zones and Records.
 */
public class DNSApi {
    private final Provider provider;
    private final ZoneApi zones;
    private final ResourceRecordSetApi.Factory rrsetApiFactory;
    private final AllProfileResourceRecordSetApi.Factory allRRSetApiFactory;
    private final GeoResourceRecordSetApi.Factory geoApiFactory;
    private final WeightedResourceRecordSetApi.Factory weightedApiFactory;

    @Inject
    DNSApi(Provider provider, ZoneApi zones, ResourceRecordSetApi.Factory rrsetApiFactory,
            AllProfileResourceRecordSetApi.Factory allRRSetApiFactory, GeoResourceRecordSetApi.Factory geoApiFactory,
            WeightedResourceRecordSetApi.Factory weightedApiFactory) {
        this.provider = provider;
        this.zones = zones;
        this.rrsetApiFactory = rrsetApiFactory;
        this.allRRSetApiFactory = allRRSetApiFactory;
        this.geoApiFactory = geoApiFactory;
        this.weightedApiFactory = weightedApiFactory;
    }

    /**
     * controls DNS zones, such as {@code netflix.com.}, availing information
     * about name servers.
     */
    public ZoneApi zones() {
        return zones;
    }

    /**
     * Controls basic DNS records as a set. Operates against the zone with id
     * {@code zoneId}. This api will not affect or return advanced records such
     * as {@link Geo}, and it is expected that no record sets returned will
     * contain a {@link ResourceRecordSet#qualifier()}. This api is supported
     * by all {@link Provider providers}.
     * 
     * <h4>Usage</h4>
     * 
     * The argument to this is {@code zoneId}. It is possible that some zones do
     * not have an id, and in this case the name is used. The following form
     * will ensure you get a reference regardless.
     * 
     * <pre>
     * api.basicRecordSetsInZone(zone.idOrName());
     * </pre>
     * 
     * <h4>Beta</h4>
     * 
     * This is marked beta until the denominator 2.0 model is finalized. If this
     * interface is unaffected following that, we'll remove the Beta status.
     * 
     * @param idOrName
     *            id of the zone, or its name if absent.
     * @see Zone#idOrName
     */
    @Beta
    public ResourceRecordSetApi basicRecordSetsInZone(String idOrName) {
        return rrsetApiFactory.create(idOrName);
    }

    /**
     * Controls all DNS records as a set. Operates against the zone with id
     * {@code zoneId}. This is supported by all {@link Provider providers}, but
     * may only return basic records, if that's all that is supported.
     * 
     * <h4>Usage</h4>
     * 
     * The argument to this is {@code zoneId}. It is possible that some zones do
     * not have an id, and in this case the name is used. The following form
     * will ensure you get a reference regardless.
     * 
     * <pre>
     * api.recordSetsInZone(zone.idOrName());
     * </pre>
     * 
     * <h4>Beta</h4>
     * 
     * This is marked beta until the denominator 2.0 model is finalized. If this
     * interface is unaffected following that, we'll remove the Beta status.
     * 
     * @param idOrName
     *            id of the zone, or its name if absent.
     * @see Zone#idOrName
     */
    public AllProfileResourceRecordSetApi recordSetsInZone(String idOrName) {
        return allRRSetApiFactory.create(idOrName);
    }

    /**
     * Controls DNS records which take into consideration the territory of the
     * caller. These are otherwise known as Directional records.
     * 
     * <h4>Usage</h4>
     * 
     * The argument to this is {@code zoneId}. It is possible that some zones do
     * not have an id, and in this case the name is used. The following form
     * will ensure you get a reference regardless.
     * 
     * <pre>
     * api.geoRecordSetsInZone(zone.idOrName());
     * </pre>
     * 
     * <h4>Beta</h4>
     * 
     * This is marked beta until the denominator 2.0 model is finalized. If this
     * interface is unaffected following that, we'll remove the Beta status.
     * 
     * @param idOrName
     *            id of the zone, or its name if absent.
     * @see Zone#idOrName
     */
    public Optional<GeoResourceRecordSetApi> geoRecordSetsInZone(String idOrName) {
        return geoApiFactory.create(idOrName);
    }

    /**
     * Controls DNS records which take into consideration the load of traffic
     * from the caller. These are otherwise known as weighted records.
     * 
     * <h4>Usage</h4>
     * 
     * The argument to this is {@code zoneId}. It is possible that some zones do
     * not have an id, and in this case the name is used. The following form
     * will ensure you get a reference regardless.
     * 
     * <pre>
     * api.weightedRecordSetsInZone(zone.idOrName());
     * </pre>
     * 
     * <h4>Beta</h4>
     * 
     * This is marked beta until the denominator 2.0 model is finalized. If this
     * interface is unaffected following that, we'll remove the Beta status.
     * 
     * @param idOrName
     *            id of the zone, or its name if absent.
     * @see Zone#idOrName
     */
    public Optional<WeightedResourceRecordSetApi> weightedRecordSetsInZone(String idOrName) {
        return weightedApiFactory.create(idOrName);
    }

    /**
     * if the provider supports duplicate zone names, we'll choose the first.
     */
    String idOrName(String zoneName) {
        if (!provider.supportsDuplicateZoneNames())
            return zoneName;
        List<Zone> currentZones = ImmutableList.copyOf(zones);
        Optional<Zone> zone = tryFind(currentZones, Zones.nameEqualTo(zoneName));
        checkArgument(zone.isPresent(), "zone %s not found in %s", zoneName, currentZones);
        return zone.get().id().get();
    }
}
