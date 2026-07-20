export type RideContext = {
  destination: string;
  journeyDurationMinutes: number;
  weather: string;
  travelPurpose: string;
};

export const DEFAULT_RIDE_CONTEXT: RideContext = {
  destination: "Airport",
  journeyDurationMinutes: 35,
  weather: "Clear",
  travelPurpose: "Leisure"
};

export const ASSIGNED_CAB = {
  id: "IRM-CAB-7421",
  driverName: "Your ride",
  availableProductIds: [
    "20000000-0000-0000-0000-000000000001",
    "20000000-0000-0000-0000-000000000002",
    "20000000-0000-0000-0000-000000000003",
    "20000000-0000-0000-0000-000000000004",
    "20000000-0000-0000-0000-000000000005",
    "20000000-0000-0000-0000-000000000006",
    "20000000-0000-0000-0000-000000000007",
    "20000000-0000-0000-0000-000000000008",
    "21000000-0000-0000-0000-000000000001",
    "21000000-0000-0000-0000-000000000002",
    "21000000-0000-0000-0000-000000000003",
    "21000000-0000-0000-0000-000000000004",
    "21000000-0000-0000-0000-000000000005",
    "21000000-0000-0000-0000-000000000006",
    "21000000-0000-0000-0000-000000000007",
    "21000000-0000-0000-0000-000000000008",
    "21000000-0000-0000-0000-000000000009",
    "21000000-0000-0000-0000-000000000010",
    "21000000-0000-0000-0000-000000000011",
    "21000000-0000-0000-0000-000000000012",
    "21000000-0000-0000-0000-000000000013",
    "21000000-0000-0000-0000-000000000014",
    "21000000-0000-0000-0000-000000000015",
    "21000000-0000-0000-0000-000000000016",
    "21000000-0000-0000-0000-000000000017",
    "21000000-0000-0000-0000-000000000018",
    "21000000-0000-0000-0000-000000000019",
    "21000000-0000-0000-0000-000000000020",
    "21000000-0000-0000-0000-000000000021",
    "21000000-0000-0000-0000-000000000022",
    "21000000-0000-0000-0000-000000000023",
    "21000000-0000-0000-0000-000000000024",
    "21000000-0000-0000-0000-000000000025",
    "21000000-0000-0000-0000-000000000026",
    "21000000-0000-0000-0000-000000000027",
    "21000000-0000-0000-0000-000000000028"
  ]
} as const;

const RIDE_CONTEXT_KEY = "inridemart.ride-context";

export function readRideContext(): RideContext {
  if (typeof window === "undefined") return DEFAULT_RIDE_CONTEXT;
  try {
    const saved = window.sessionStorage.getItem(RIDE_CONTEXT_KEY);
    if (!saved) return DEFAULT_RIDE_CONTEXT;
    const value = JSON.parse(saved) as Partial<RideContext>;
    return {
      destination: value.destination?.trim() || DEFAULT_RIDE_CONTEXT.destination,
      journeyDurationMinutes: clampDuration(value.journeyDurationMinutes),
      weather: value.weather?.trim() || DEFAULT_RIDE_CONTEXT.weather,
      travelPurpose: value.travelPurpose?.trim() || DEFAULT_RIDE_CONTEXT.travelPurpose
    };
  } catch {
    return DEFAULT_RIDE_CONTEXT;
  }
}

export function saveRideContext(context: RideContext): void {
  window.sessionStorage.setItem(RIDE_CONTEXT_KEY, JSON.stringify({
    ...context,
    journeyDurationMinutes: clampDuration(context.journeyDurationMinutes)
  }));
}

export function isAvailableInAssignedCab(productId: string): boolean {
  return ASSIGNED_CAB.availableProductIds.includes(productId as never);
}

function clampDuration(value: number | undefined): number {
  if (!Number.isFinite(value)) return DEFAULT_RIDE_CONTEXT.journeyDurationMinutes;
  return Math.min(240, Math.max(5, Math.round(value as number)));
}
