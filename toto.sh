#!/bin/bash

echo "🔧 === POKEMON CARD PLANNING BUILD DIAGNOSTIC ==="
echo "Timestamp: $(date)"
echo

# Function to print colored output
print_status() {
    echo -e "\033[1;34m$1\033[0m"
}

print_error() {
    echo -e "\033[1;31m$1\033[0m"
}

print_success() {
    echo -e "\033[1;32m$1\033[0m"
}

print_warning() {
    echo -e "\033[1;33m$1\033[0m"
}

# 1. Check Java version
print_status "=== CHECKING JAVA ENVIRONMENT ==="
echo "Java version:"
java -version
echo

echo "JAVA_HOME: ${JAVA_HOME:-'Not set'}"
echo

# 2. Check Maven version
print_status "=== CHECKING MAVEN ENVIRONMENT ==="
mvn -version
echo

# 3. Check project structure
print_status "=== CHECKING PROJECT STRUCTURE ==="
echo "Current directory: $(pwd)"
echo

if [ -f "pom.xml" ]; then
    print_success "✅ pom.xml found"
else
    print_error "❌ pom.xml not found"
    exit 1
fi

if [ -f "src/main/java/com/pcagrade/order/OrderManagementApplication.java" ]; then
    print_success "✅ Main class found"
else
    print_error "❌ Main class not found"
    echo "Looking for main class..."
    find src -name "*.java" -type f | grep -i application | head -5
fi

# 4. Check application.properties
print_status "=== CHECKING CONFIGURATION FILES ==="
if [ -f "src/main/resources/application.properties" ]; then
    print_success "✅ application.properties found"
    echo "Database configuration:"
    grep -E "(spring.datasource|spring.jpa)" src/main/resources/application.properties | head -5
else
    print_warning "⚠️ application.properties not found"
fi

# 5. Clean previous builds
print_status "=== CLEANING PREVIOUS BUILDS ==="
mvn clean -q
if [ $? -eq 0 ]; then
    print_success "✅ Clean successful"
else
    print_error "❌ Clean failed"
fi

# 6. Validate POM
print_status "=== VALIDATING POM.XML ==="
mvn validate -q
if [ $? -eq 0 ]; then
    print_success "✅ POM validation successful"
else
    print_error "❌ POM validation failed"
    echo "Checking for common POM issues:"

    # Check for XML syntax issues
    echo "Checking XML syntax..."
    if command -v xmllint >/dev/null 2>&1; then
        xmllint --noout pom.xml 2>&1
    else
        echo "xmllint not available, skipping XML validation"
    fi
fi

# 7. Compile with detailed output
print_status "=== ATTEMPTING COMPILATION ==="
echo "Compiling with detailed error output..."

mvn compile -X > compile-detailed.log 2>&1
COMPILE_EXIT_CODE=$?

if [ $COMPILE_EXIT_CODE -eq 0 ]; then
    print_success "✅ COMPILATION SUCCESSFUL"
else
    print_error "❌ COMPILATION FAILED (Exit code: $COMPILE_EXIT_CODE)"
    echo
    echo "Last 20 lines of compilation log:"
    tail -20 compile-detailed.log
    echo
    echo "Full compilation log saved to: compile-detailed.log"

    # Look for common error patterns
    echo "Checking for common errors:"

    if grep -i "package.*does not exist" compile-detailed.log; then
        print_error "❌ Missing package imports detected"
    fi

    if grep -i "cannot find symbol" compile-detailed.log; then
        print_error "❌ Missing symbols/dependencies detected"
    fi

    if grep -i "incompatible types" compile-detailed.log; then
        print_error "❌ Type compatibility issues detected"
    fi

    if grep -i "duplicate class" compile-detailed.log; then
        print_error "❌ Duplicate class definitions detected"
    fi
fi

# 8. Attempt Spring Boot run
if [ $COMPILE_EXIT_CODE -eq 0 ]; then
    print_status "=== TESTING SPRING BOOT STARTUP ==="
    echo "Attempting to start application (will timeout after 30 seconds)..."

    timeout 30s mvn spring-boot:run > spring-boot-startup.log 2>&1 &
    SPRING_PID=$!

    sleep 5

    if ps -p $SPRING_PID > /dev/null 2>&1; then
        print_success "✅ Spring Boot startup initiated successfully"
        kill $SPRING_PID 2>/dev/null
        wait $SPRING_PID 2>/dev/null
    else
        print_error "❌ Spring Boot startup failed"
        echo "Spring Boot startup log:"
        cat spring-boot-startup.log
    fi
fi

# 9. Summary
print_status "=== DIAGNOSTIC SUMMARY ==="
echo "Java: $(java -version 2>&1 | head -n1)"
echo "Maven: $(mvn -version | head -n1)"
echo "Project structure: $([ -f pom.xml ] && echo "OK" || echo "ISSUE")"
echo "Configuration: $([ -f src/main/resources/application.properties ] && echo "OK" || echo "MISSING")"
echo "Compilation: $([ $COMPILE_EXIT_CODE -eq 0 ] && echo "SUCCESS" || echo "FAILED")"

echo
if [ $COMPILE_EXIT_CODE -eq 0 ]; then
    print_success "🎉 BUILD READY - You can run: mvn spring-boot:run"
else
    print_error "🚨 BUILD ISSUES DETECTED"
    echo "📋 Next steps:"
    echo "1. Check compile-detailed.log for specific errors"
    echo "2. Verify all imports and dependencies"
    echo "3. Ensure database is running (MariaDB on port 3306)"
    echo "4. Check application.properties configuration"
fi

echo
echo "🔍 Log files created:"
echo "  - compile-detailed.log (compilation details)"
echo "  - spring-boot-startup.log (startup attempt log)"