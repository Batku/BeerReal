
# Step 3 Report

## BeerReal

## Team Members

Hendrik Aarma<br>
Gloria Rannik<br>
Kevin Näälik<br>
Tamor Tomson<br>

## Which API was chosen and why
**Google Places API Service**
- **PlacesApiService Interface**: Clean API contract with GET request
- **PlacesApiServiceImpl**: 
  - OkHttp-based implementation
  - Fetches bars within 2km radius
  - Parses JSON responses into data class PlacesApiResponse
  - Proper error handling and exception propagation

## API endpoint used
https://maps.googleapis.com/maps/api/place/nearbysearch/json 

## Error handling strategy
- Toast messages for critical errors only
- Loading states during async operations
- Network error recovery with cached data
- Proper exception handling throughout
