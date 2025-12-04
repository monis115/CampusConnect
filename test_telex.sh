#!/bin/bash

echo "🧪 Testing Telex Application Endpoint"
echo "======================================"
echo ""

# Check if student ID is provided
STUDENT_ID=${1:-1}
echo "📝 Testing with Student ID: $STUDENT_ID"
echo ""

# Read the JSON data
JSON_DATA=$(cat test_telex_request.json)

# Create a small test JPEG if it doesn't exist (1x1 pixel)
if [ ! -f "test_photo.jpg" ]; then
    echo "📸 Creating test photo..."
    # Create a minimal 1x1 JPEG (base64 encoded minimal JPEG)
    echo "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0a
HBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIy
MjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAABAAEDASIA
AhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEB
AQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCwAB//2Q==" | base64 -d > test_photo.jpg
fi

if [ ! -f "test_passport.pdf" ]; then
    echo "📄 Creating test passport PDF..."
    # Create a minimal PDF
    echo "%PDF-1.0
1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj 
2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj
3 0 obj<</Type/Page/MediaBox[0 0 612 792]/Parent 2 0 R/Resources<<>>>>endobj
xref
0 4
0000000000 65535 f
0000000009 00000 n
0000000052 00000 n
0000000101 00000 n
trailer<</Size 4/Root 1 0 R>>
startxref
178
%%EOF" > test_passport.pdf
fi

echo "📤 Sending request to endpoint..."
echo ""

# Make the API call
curl -X POST "http://localhost:8080/api/agents/apply-for-telex/${STUDENT_ID}" \
  -F "details=${JSON_DATA}" \
  -F "visaPhoto=@test_photo.jpg" \
  -F "visaPassportCopy=@test_passport.pdf" \
  -F "ttCopy=@test_passport.pdf" \
  -w "\n\n📊 HTTP Status: %{http_code}\n" \
  -o response.json

echo ""
echo "✅ Response saved to: response.json"
echo ""
echo "📄 Response Content:"
cat response.json | python3 -m json.tool 2>/dev/null || cat response.json
echo ""
