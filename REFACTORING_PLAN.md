# 🔧 REFACTORING PLAN - Gym Health Tech Backend

## Executive Summary

Comprehensive refactoring plan to unify coding style, normalize naming conventions, apply consistent architecture patterns, extract reusable components, and improve folder structure while maintaining Single Responsibility Principle.

---

## 1. ISSUES FOUND

### 1.1 Architecture & Folder Structure Issues

#### ❌ **Issue 1.1.1**: Architecture Mismatch

- **Current**: Traditional Spring Boot layered architecture (Controller → Service → Repository)
- **Expected** (per DEVELOPER_GUIDE.md): Clean Architecture with domain/application/adapter/infrastructure layers
- **Impact**: Code organization doesn't match documented architecture
- **Files Affected**: All packages (currently flat structure)

#### ❌ **Issue 1.1.2**: Inconsistent Package Organization

- Services, DTOs, Models, Repositories all in root package
- No clear separation between domain, application, and infrastructure
- **Files Affected**: All Java files in `com.thentrees.gymhealthtech`

### 1.2 Naming Convention Issues

#### ❌ **Issue 1.2.1**: Inconsistent Mapper Method Names

- `mapToResponse()` - FoodServiceImpl, UserProfileServiceImpl, EquipmentServiceImpl
- `convertToResponse()` - CustomPlanServiceImpl, SessionManagementServiceImpl
- `toResponse()` - PostMapper (MapStruct)
- `from()` - GoalResponse (static factory)
- **Files Affected**:
  - `FoodServiceImpl.java`
  - `UserProfileServiceImpl.java`
  - `EquipmentServiceImpl.java`
  - `CustomPlanServiceImpl.java`
  - `SessionManagementServiceImpl.java`
  - `PostMapper.java`
  - `GoalResponse.java`

#### ❌ **Issue 1.2.2**: Utility Class Naming

- Utility classes using verbs: `GetClientIp`, `GetRequestDetails`, `GenerateTraceId`, `ExtractValidationErrors`
- Should follow noun pattern: `ClientIpExtractor`, `RequestDetailsExtractor`, etc.
- **Files Affected**:
  - `GetClientIp.java`
  - `GetRequestDetails.java`
  - `GenerateTraceId.java`
  - `ExtractValidationErrors.java`
  - `DetermineHttpStatus.java`
  - `FormatFileSize.java`

#### ❌ **Issue 1.2.3**: Mixed Response DTO Patterns

- Some use `Response` suffix (e.g., `UserResponse`, `PlanResponse`)
- Some use `DTO` suffix (e.g., `UserSummaryDTO`, `PlanSummaryDTO`)
- Should be consistent: all use `Response` suffix
- **Files Affected**:
  - `UserSummaryDTO.java` → should be `UserSummaryResponse.java`
  - `PlanSummaryDTO.java` → should be `PlanSummaryResponse.java`

### 1.3 Code Duplication Issues

#### ❌ **Issue 1.3.1**: Duplicated Entity-to-DTO Mapping Logic

- Manual mapping repeated across multiple services
- Similar conversion patterns in CustomPlanServiceImpl, SessionManagementServiceImpl, TemplateWorkoutServiceImpl
- **Files Affected**:
  - `CustomPlanServiceImpl.java` (lines 536-666)
  - `SessionManagementServiceImpl.java` (lines 810-860)
  - `TemplateWorkoutServiceImpl.java` (lines 347-451)
  - `FoodServiceImpl.java` (lines 246-315)
  - `UserProfileServiceImpl.java` (line 192)
  - `EquipmentServiceImpl.java` (line 160)

#### ❌ **Issue 1.3.2**: Duplicated Prescription Conversion

- `convertPrescriptionToJson()` duplicated in:
  - `CustomPlanServiceImpl.java` (line 524)
  - `TemplateWorkoutServiceImpl.java` (line 451)
- **Files Affected**: Both files above

#### ❌ **Issue 1.3.3**: Duplicated User Extraction from Authentication

- Pattern `User user = (User) authentication.getPrincipal();` repeated in 20+ locations
- **Files Affected**: Most controllers and services

#### ❌ **Issue 1.3.4**: Duplicated Verification Token Generation

- `generateAndSendVerificationToken()` in:
  - `UserRegistrationServiceImpl.java` (line 118)
  - `AuthenticationServiceImpl.java` (line 317)
- **Files Affected**: Both files above

### 1.4 Single Responsibility Violations

#### ❌ **Issue 1.4.1**: Service Classes Doing Too Much

- `CustomPlanServiceImpl.java`: 700+ lines, handles CRUD, validation, mapping, business logic
- `SessionManagementServiceImpl.java`: 900+ lines, handles sessions, summaries, conversions
- `AIServiceImpl.java`: Contains mapping logic that should be in mapper
- **Files Affected**:
  - `CustomPlanServiceImpl.java`
  - `SessionManagementServiceImpl.java`
  - `AIServiceImpl.java`

#### ❌ **Issue 1.4.2**: Utility Classes as Components

- Simple utility methods converted to `@Component` unnecessarily
- `GetClientIp`, `GetRequestDetails`, `GenerateTraceId` could be static utility classes
- Only utilities needing dependency injection should be components
- **Files Affected**:
  - `GetClientIp.java` → can be static
  - `GetRequestDetails.java` → can be static
  - `GenerateTraceId.java` → can be static
  - `FormatFileSize.java` → can be static
  - `DetermineHttpStatus.java` → keep as component (uses ErrorCodes)
  - `CacheKeyUtils.java` → keep as component (needs ObjectMapper)
  - `S3Util.java` → keep as component (needs S3Client)

### 1.5 Inconsistent Patterns

#### ❌ **Issue 1.5.1**: Mixed Mapping Strategies

- MapStruct for Posts/Comments
- Manual mapping for Plans/Sessions/Foods
- Static factory methods for Goals
- Should standardize on MapStruct for complex mappings
- **Files Affected**: All service implementations with mapping

#### ❌ **Issue 1.5.2**: Inconsistent Exception Handling

- Some services throw `BusinessException`
- Some throw `ResourceNotFoundException`
- Some throw domain-specific exceptions
- **Files Affected**: All service implementations

#### ❌ **Issue 1.5.3**: Inconsistent Logging

- Some use `@Slf4j` with topic: `@Slf4j(topic = "GOAL-SERVICE")`
- Some use default: `@Slf4j`
- **Files Affected**: All service implementations

### 1.6 Code Quality Issues

#### ❌ **Issue 1.6.1**: Missing Builder Pattern Consistency

- Some DTOs use `@Builder` with `@AllArgsConstructor` and `@NoArgsConstructor`
- Some only use `@Builder`
- **Files Affected**: All DTO classes

#### ❌ **Issue 1.6.2**: Inconsistent Error Messages

- Mix of Vietnamese and English error messages
- **Files Affected**: Exception classes, constant files

#### ❌ **Issue 1.6.3**: Commented Code

- `S3Util.java` line 45: commented code `// return getFileUrl(s3Key);`
- **Files Affected**: `S3Util.java`

---

## 2. REFACTOR STRATEGY

### 2.1 Phase 1: Naming Standardization

#### Strategy 1.1: Unify Mapper Method Names

- **Standard**: Use `toResponse()` for all entity-to-DTO conversions
- **Action**: Rename all `mapToResponse()` and `convertToResponse()` to `toResponse()`
- **Implementation**:
  - Create MapStruct mappers where possible
  - Keep manual mapping for complex cases but use consistent naming

#### Strategy 1.2: Rename Utility Classes

- **Standard**: Use noun-based names for utility classes
- **Action**:
  - `GetClientIp` → `ClientIpExtractor`
  - `GetRequestDetails` → `RequestDetailsExtractor`
  - `GenerateTraceId` → `TraceIdGenerator`
  - `ExtractValidationErrors` → `ValidationErrorExtractor`
  - `DetermineHttpStatus` → `HttpStatusResolver`
  - `FormatFileSize` → `FileSizeFormatter`

#### Strategy 1.3: Standardize DTO Naming

- **Action**: Rename `*DTO` to `*Response`
  - `UserSummaryDTO` → `UserSummaryResponse`
  - `PlanSummaryDTO` → `PlanSummaryResponse`

### 2.2 Phase 2: Extract Reusable Components

#### Strategy 2.1: Create Shared Mapper Utilities

- **Action**: Create `EntityMapper` base utility with common mapping patterns
- **Location**: `com.thentrees.gymhealthtech.mapper.common`
- **Functions**:
  - Prescription conversion
  - Exercise mapping
  - User summary extraction

#### Strategy 2.2: Create Authentication Utility

- **Action**: Create `AuthenticationHelper` to extract user from Authentication
- **Location**: `com.thentrees.gymhealthtech.util`
- **Function**: `getCurrentUser(Authentication auth)`

#### Strategy 2.3: Extract Verification Service

- **Action**: Create `VerificationTokenService` to handle token generation
- **Location**: `com.thentrees.gymhealthtech.service`
- **Consolidate**: Both `generateAndSendVerificationToken()` implementations

### 2.3 Phase 3: Simplify Utility Classes

#### Strategy 3.1: Convert to Static Utilities

- **Action**: Convert stateless utilities to static classes
- **Pattern**: `public final class ClientIpExtractor { private ClientIpExtractor() {} public static String extract(HttpServletRequest request) {...} }`
- **Affected**:
  - `ClientIpExtractor` (formerly `GetClientIp`)
  - `RequestDetailsExtractor` (formerly `GetRequestDetails`)
  - `TraceIdGenerator` (formerly `GenerateTraceId`)
  - `FileSizeFormatter` (formerly `FormatFileSize`)

#### Strategy 3.2: Keep Component Utilities

- **Keep as @Component**:
  - `CacheKeyUtils` (needs ObjectMapper)
  - `S3Util` (needs S3Client)
  - `HttpStatusResolver` (uses constants but keeps for consistency)
  - `ValidationErrorExtractor` (may need expansion)

### 2.4 Phase 4: Implement MapStruct Consistently

#### Strategy 4.1: Create MapStruct Mappers

- **Action**: Create MapStruct mappers for all major entities
- **Location**: `com.thentrees.gymhealthtech.mapper`
- **Priority**:
  1. `PlanMapper` (for CustomPlanServiceImpl)
  2. `SessionMapper` (for SessionManagementServiceImpl)
  3. `FoodMapper` (already exists, enhance)
  4. `TemplateWorkoutMapper` (for TemplateWorkoutServiceImpl)
  5. `EquipmentMapper` (for EquipmentServiceImpl)
  6. `UserProfileMapper` (for UserProfileServiceImpl)

#### Strategy 4.2: Migrate Manual Mappings

- **Action**: Replace manual mappings with MapStruct
- **Keep manual**: Only for very complex mappings requiring business logic

### 2.5 Phase 5: Improve Service Layer

#### Strategy 5.1: Split Large Services

- **Action**: Break down services into focused classes
- **CustomPlanService**:
  - `PlanQueryService` - read operations
  - `PlanCommandService` - write operations
  - `PlanValidationService` - validation logic
- **SessionManagementService**:
  - `SessionQueryService` - read operations
  - `SessionCommandService` - write operations
  - `SessionSummaryService` - summary calculations

#### Strategy 5.2: Extract Mapping to Mapper Layer

- **Action**: Move all mapping logic from services to mapper classes
- **Pattern**: Services call mappers, not manual conversion

### 2.6 Phase 6: Standardize Patterns

#### Strategy 6.1: Unified Logging

- **Action**: All services use `@Slf4j(topic = "SERVICE-NAME")`
- **Pattern**: `@Slf4j(topic = "PLAN-SERVICE")`, `@Slf4j(topic = "SESSION-SERVICE")`

#### Strategy 6.2: Unified Exception Handling

- **Action**: Standardize exception types per layer
- **Pattern**:
  - Controllers: catch and wrap
  - Services: throw domain exceptions
  - Repositories: throw data exceptions

#### Strategy 6.3: Unified DTO Patterns

- **Action**: All DTOs use:
  ```java
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  ```

---

## 3. FILES IMPACTED

### 3.1 High Priority (Core Refactoring)

#### Utility Classes (12 files)

- `src/main/java/com/thentrees/gymhealthtech/util/GetClientIp.java` → Rename + Static
- `src/main/java/com/thentrees/gymhealthtech/util/GetRequestDetails.java` → Rename + Static
- `src/main/java/com/thentrees/gymhealthtech/util/GenerateTraceId.java` → Rename + Static
- `src/main/java/com/thentrees/gymhealthtech/util/FormatFileSize.java` → Rename + Static
- `src/main/java/com/thentrees/gymhealthtech/util/ExtractValidationErrors.java` → Rename
- `src/main/java/com/thentrees/gymhealthtech/util/DetermineHttpStatus.java` → Rename
- `src/main/java/com/thentrees/gymhealthtech/util/CacheKeyUtils.java` → Keep as-is
- `src/main/java/com/thentrees/gymhealthtech/util/S3Util.java` → Remove commented code

#### Service Implementations (10 files)

- `src/main/java/com/thentrees/gymhealthtech/service/impl/CustomPlanServiceImpl.java` → Split + Extract mapping
- `src/main/java/com/thentrees/gymhealthtech/service/impl/SessionManagementServiceImpl.java` → Split + Extract mapping
- `src/main/java/com/thentrees/gymhealthtech/service/impl/FoodServiceImpl.java` → Use mapper
- `src/main/java/com/thentrees/gymhealthtech/service/impl/TemplateWorkoutServiceImpl.java` → Use mapper
- `src/main/java/com/thentrees/gymhealthtech/service/impl/EquipmentServiceImpl.java` → Use mapper
- `src/main/java/com/thentrees/gymhealthtech/service/impl/UserProfileServiceImpl.java` → Use mapper
- `src/main/java/com/thentrees/gymhealthtech/service/impl/AIServiceImpl.java` → Extract mapping
- `src/main/java/com/thentrees/gymhealthtech/service/impl/UserRegistrationServiceImpl.java` → Use VerificationService
- `src/main/java/com/thentrees/gymhealthtech/service/impl/AuthenticationServiceImpl.java` → Use VerificationService
- `src/main/java/com/thentrees/gymhealthtech/service/impl/ExerciseLibraryServiceImpl.java` → Use mapper consistently

#### DTOs (2 files)

- `src/main/java/com/thentrees/gymhealthtech/dto/response/UserSummaryDTO.java` → Rename to `UserSummaryResponse.java`
- `src/main/java/com/thentrees/gymhealthtech/dto/response/PlanSummaryDTO.java` → Rename to `PlanSummaryResponse.java`

### 3.2 Medium Priority (Consistency Improvements)

#### Mapper Classes (6 files to create, 3 to enhance)

- **New**: `src/main/java/com/thentrees/gymhealthtech/mapper/PlanMapper.java`
- **New**: `src/main/java/com/thentrees/gymhealthtech/mapper/SessionMapper.java`
- **New**: `src/main/java/com/thentrees/gymhealthtech/mapper/TemplateWorkoutMapper.java`
- **New**: `src/main/java/com/thentrees/gymhealthtech/mapper/EquipmentMapper.java`
- **New**: `src/main/java/com/thentrees/gymhealthtech/mapper/UserProfileMapper.java`
- **New**: `src/main/java/com/thentrees/gymhealthtech/mapper/common/EntityMapper.java`
- **Enhance**: `src/main/java/com/thentrees/gymhealthtech/mapper/FoodMapper.java`
- **Enhance**: `src/main/java/com/thentrees/gymhealthtech/mapper/ExerciseMapper.java`

#### Controllers (14 files)

- All controllers need updates to use renamed utilities and services
- Particularly: `UserController.java`, `PlanController.java`, `SessionController.java`

### 3.3 Low Priority (Cleanup)

#### Configuration Files

- Update imports in config classes using renamed utilities

#### Exception Classes

- Standardize error messages (Vietnamese vs English)

---

## 4. IMPLEMENTATION PRIORITY

### Phase 1: Quick Wins (Week 1)

1. Rename utility classes
2. Convert utilities to static where possible
3. Rename DTO classes
4. Remove commented code

### Phase 2: Core Refactoring (Week 2-3)

1. Create shared mapper utilities
2. Create MapStruct mappers
3. Extract AuthenticationHelper
4. Extract VerificationTokenService

### Phase 3: Service Improvements (Week 4-5)

1. Migrate services to use mappers
2. Split large services
3. Standardize logging

### Phase 4: Final Cleanup (Week 6)

1. Update all references
2. Update tests
3. Documentation updates

---

## 5. RISKS & MITIGATION

### Risk 1: Breaking Changes

- **Mitigation**: Refactor incrementally, maintain backward compatibility where possible

### Risk 2: Test Coverage

- **Mitigation**: Run all tests after each phase, fix breaking tests immediately

### Risk 3: Large File Changes

- **Mitigation**: Break down into smaller PRs, review carefully

---

## 6. SUCCESS CRITERIA

✅ All utility classes follow consistent naming (noun-based)
✅ All mapper methods use `toResponse()` naming
✅ No duplicated mapping logic
✅ All services use MapStruct or centralized mappers
✅ Large services split into focused classes
✅ All DTOs follow consistent pattern
✅ Logging standardized across services
✅ No commented code
✅ All static utilities are truly static

---

**Estimated Total Impact**: ~100 files (renames, refactors, new files)
**Estimated Time**: 4-6 weeks (incremental approach)
**Breaking Changes**: Minimal (mostly internal refactoring)
