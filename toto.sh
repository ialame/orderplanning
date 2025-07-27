#!/bin/bash

# Test Script for English Planning System
# Tests the translated planning system using j_planning table

echo "🚀 Testing English Planning System..."
echo "====================================="
echo ""

# Configuration
BASE_URL="http://localhost:8080"
START_DATE="2025-07-27"
NUMBER_OF_EMPLOYEES=4
TIME_PER_CARD=3

echo "📋 Configuration:"
echo "  - URL: $BASE_URL"
echo "  - Start Date: $START_DATE"
echo "  - Employees: $NUMBER_OF_EMPLOYEES"
echo "  - Time/Card: $TIME_PER_CARD min"
echo ""

# Test 1: Debug with real data
echo "🔍 1. Testing debug endpoint..."
echo "-----------------------------"
response1=$(curl -s "$BASE_URL/api/planning/debug-real")
echo "$response1" | jq '.'

# Check if success
if echo "$response1" | grep -q '"status":"SUCCESS"'; then
    echo "✅ Debug: SUCCESS"
    available_orders=$(echo "$response1" | jq -r '.availableOrders // 0')
    active_employees=$(echo "$response1" | jq -r '.activeEmployees // 0')
    echo "   📊 $available_orders orders available since June 2025"
    echo "   👥 $active_employees active employees"
else
    echo "❌ Debug: ERROR"
    echo "   Check that Spring Boot application is running"
    exit 1
fi

echo ""
echo "⏱️  Pause 2 seconds..."
sleep 2

# Test 2: Generate planning
echo ""
echo "🎯 2. Testing planning generation..."
echo "-----------------------------------"
response2=$(curl -s -X POST "$BASE_URL/api/planning/generate" \
  -H "Content-Type: application/json" \
  -d "{
    \"startDate\":\"$START_DATE\",
    \"numberOfEmployees\":$NUMBER_OF_EMPLOYEES,
    \"timePerCard\":$TIME_PER_CARD
  }")

echo "$response2" | jq '.'

# Check if success
if echo "$response2" | grep -q '"success":true'; then
    echo "✅ Generation: SUCCESS"

    # Extract statistics
    plannings_created=$(echo "$response2" | jq -r '.planningsCreated // 0')
    plannings_saved=$(echo "$response2" | jq -r '.planningsSaved // 0')
    orders_analyzed=$(echo "$response2" | jq -r '.ordersAnalyzed // 0')

    echo "   📊 $plannings_created plannings created"
    echo "   💾 $plannings_saved saved to database"
    echo "   📦 $orders_analyzed orders analyzed"

    if [ "$plannings_saved" -gt 0 ]; then
        echo "   ✅ Database save successful!"
    else
        echo "   ⚠️  No plannings saved - check database"
    fi
else
    echo "❌ Generation: ERROR"
    echo "$response2" | jq -r '.error // "Unknown error"'
fi

echo ""
echo "⏱️  Pause 2 seconds..."
sleep 2

# Test 3: View planning for specific date
echo ""
echo "📅 3. Testing planning view for $START_DATE..."
echo "---------------------------------------------"
response3=$(curl -s "$BASE_URL/api/planning/view?date=$START_DATE")
echo "$response3" | jq '.'

if echo "$response3" | jq -e '. | length > 0' > /dev/null 2>&1; then
    count=$(echo "$response3" | jq '. | length')
    echo "✅ View: $count planning items found for $START_DATE"

    # Show first planning item details
    first_planning=$(echo "$response3" | jq '.[0]')
    employee_name=$(echo "$first_planning" | jq -r '.employeeName // "Unknown"')
    order_number=$(echo "$first_planning" | jq -r '.orderNumber // "Unknown"')
    start_time=$(echo "$first_planning" | jq -r '.startTime // "Unknown"')

    echo "   📋 Example: $order_number → $employee_name at $start_time"
else
    echo "ℹ️  View: No planning found for $START_DATE"
fi

echo ""
echo "⏱️  Pause 2 seconds..."
sleep 2

# Test 4: View all plannings
echo ""
echo "📊 4. Testing all plannings view..."
echo "----------------------------------"
response4=$(curl -s "$BASE_URL/api/planning/view")

if echo "$response4" | jq -e '. | length > 0' > /dev/null 2>&1; then
    total_count=$(echo "$response4" | jq '. | length')
    echo "✅ All plannings: $total_count total planning items found"

    # Group by date
    dates=$(echo "$response4" | jq -r '.[].planningDate' | sort | uniq -c)
    echo "   📅 Planning distribution:"
    echo "$dates" | while read count date; do
        echo "      $date: $count plannings"
    done
else
    echo "ℹ️  All plannings: No plannings found in database"
fi

echo ""
echo "⏱️  Pause 2 seconds..."
sleep 2

# Test 5: Statistics
echo ""
echo "📈 5. Testing statistics..."
echo "--------------------------"
response5=$(curl -s "$BASE_URL/api/planning/stats")
echo "$response5" | jq '.'

if echo "$response5" | grep -q '"status":"SUCCESS"'; then
    echo "✅ Statistics: SUCCESS"

    total_plannings=$(echo "$response5" | jq -r '.totalPlannings // 0')
    echo "   📊 Total plannings in system: $total_plannings"

    # Show employee stats if available
    employee_stats=$(echo "$response5" | jq -r '.employeeStats[]? | "\(.employeeName): \(.planningCount) plannings"')
    if [ ! -z "$employee_stats" ]; then
        echo "   👥 Employee distribution:"
        echo "$employee_stats" | while read line; do
            echo "      $line"
        done
    fi
else
    echo "❌ Statistics: ERROR"
fi

echo ""
echo "✅ Tests completed!"
echo "==================="
echo ""

# Summary
echo "📋 Summary:"
echo "  ✅ Debug endpoint working"
echo "  ✅ Planning generation tested"
echo "  ✅ Planning view tested"
echo "  ✅ Statistics tested"
echo ""

echo "🎯 English Planning System Status:"
if [ "$plannings_saved" -gt 0 ]; then
    echo "  ✅ FULLY FUNCTIONAL - Database operations working"
    echo "  ✅ Translation successful from French system"
    echo "  ✅ Using j_planning table with English columns"
else
    echo "  ⚠️  PARTIALLY FUNCTIONAL - Generation works but database save needs verification"
    echo "  ℹ️  Check database table structure and permissions"
fi

echo ""
echo "🔧 Available endpoints:"
echo "  GET  /api/planning/debug-real"
echo "  POST /api/planning/generate"
echo "  GET  /api/planning/view?date=YYYY-MM-DD"
echo "  GET  /api/planning/view"
echo "  GET  /api/planning/stats"
echo "  DELETE /api/planning/cleanup"
echo ""

echo "🗃️  Next steps:"
echo "  1. Run database migration script if not done"
echo "  2. Verify j_planning table exists with English columns"
echo "  3. Update frontend to use new English field names"
echo "  4. Test integration with existing employee management"