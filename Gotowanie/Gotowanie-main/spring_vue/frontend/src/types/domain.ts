// Mirrors the backend domain model. Domain-specific attributes live in `metadata`,
// keeping these types generic across any subject.

export type ResourceStatus = 'ACTIVE' | 'INACTIVE' | 'UNAVAILABLE'

export type RequestStatus =
  | 'DRAFT'
  | 'PENDING'
  | 'CONFIRMED'
  | 'CANCELLED'
  | 'REJECTED'
  | 'COMPLETED'

export type FieldType = 'TEXT' | 'TEXTAREA' | 'NUMBER' | 'BOOLEAN' | 'DATE' | 'SELECT'

export interface DomainFieldConfig {
  key: string
  label: string
  type: string
  required: boolean
  options: string[]
  helpText?: string | null
}

export type MetadataValue = string | number | boolean | null
export type Metadata = Record<string, MetadataValue>

export interface Resource {
  id: number
  name: string
  description: string | null
  type: string | null
  status: ResourceStatus
  baseValue: number | null
  capacityValue: number | null
  metadata: Metadata
  createdAt: string
  updatedAt: string
}

export interface BreakdownLine {
  label: string
  amount: number
  detail: string
}

export interface AlgorithmBreakdown {
  lines: BreakdownLine[]
  appliedRules: string[]
  notes: string[]
  total: number
  currency: string
}

export interface RequestItem {
  id: number
  ownerId: number
  ownerEmail: string | null
  resourceId: number
  resourceName: string | null
  status: RequestStatus
  startAt: string | null
  endAt: string | null
  quantity: number | null
  calculatedValue: number | null
  currency: string
  metadata: Metadata
  algorithmBreakdown: AlgorithmBreakdown | null
  createdAt: string
  updatedAt: string
}

export interface CreateRequestPayload {
  resourceId: number
  startAt?: string | null
  endAt?: string | null
  quantity?: number | null
  metadata?: Metadata
}

export interface CreateResourcePayload {
  name: string
  description?: string | null
  type?: string | null
  status?: ResourceStatus
  baseValue?: number | null
  capacityValue?: number | null
  metadata?: Metadata
}

/** Shape of the active domain profile returned by GET /api/config. */
export interface DomainProfile {
  domainName: string
  resourceLabelSingular: string
  resourceLabelPlural: string
  requestLabelSingular: string
  requestLabelPlural: string
  currency: string
  algorithmMode: string
  pricingUnit: string
  requiresTimeWindow: boolean
  requiresQuantity: boolean
  resourceFields: DomainFieldConfig[]
  requestFields: DomainFieldConfig[]
}

export interface DomainConfigView {
  profile: DomainProfile
  roles: string[]
  resourceStatuses: ResourceStatus[]
  requestStatuses: RequestStatus[]
}

export interface DomainUiConfig {
  appName: string
  currency: string
  requiresTimeWindow: boolean
  requiresQuantity: boolean
  timeMode: 'slots' | 'range' | 'none'
  uiLayout: 'list' | 'cards'
  resource: {
    singular: string
    plural: string
    fields: DomainFieldConfig[]
  }
  request: {
    singular: string
    plural: string
    fields: DomainFieldConfig[]
  }
  labels: Record<string, string>
}

export interface DomainUiConfig {
  appName: string
  currency: string
  requiresTimeWindow: boolean
  requiresQuantity: boolean
  timeMode: 'slots' | 'range' | 'none'
  uiLayout: 'list' | 'cards'
  resource: {
    singular: string
    plural: string
    fields: DomainFieldConfig[]
  }
  request: {
    singular: string
    plural: string
    fields: DomainFieldConfig[]
  }
  labels: Record<string, string>
}
