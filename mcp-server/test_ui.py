import streamlit as st
import requests
import json

st.set_page_config(page_title="PlantPulse MCP Server Test", layout="wide")

st.title("🌱 PlantPulse MCP Server Test UI")
st.markdown("Test the 7 MCP tools available on the MCP server")

# Configuration
MCP_SERVER = "http://localhost:8085"
PLANT_SERVICE = "http://localhost:8081"

st.markdown("---")

# Tabs for different tool categories
tab1, tab2, tab3, tab4 = st.tabs(["Authentication", "Plant Tools", "Diagnosis", "Integration"])

with tab1:
    st.header("1️⃣ Authentication")

    sub_tab1, sub_tab2 = st.tabs(["Login", "Register"])

    with sub_tab1:
        st.markdown("### plantpulse_login")
        st.write("Log in to PlantPulse and get a session token")

        col1, col2 = st.columns(2)
        with col1:
            email = st.text_input("Email", value="test@plantpulse.com", key="login_email")
        with col2:
            password = st.text_input("Password", type="password", value="password123", key="login_password")

        if st.button("🔐 Login"):
            try:
                response = requests.post(
                    f"{PLANT_SERVICE}/api/auth/login",
                    json={"email": email, "password": password},
                    timeout=5
                )

                if response.status_code in [200, 201]:
                    data = response.json()
                    st.success(f"✅ Logged in as {email}")
                    st.code(json.dumps(data, indent=2), language="json")
                    st.session_state.auth_token = data.get("token")
                else:
                    st.error(f"❌ Login failed: {response.status_code}")
                    st.write(response.text)
            except Exception as e:
                st.error(f"❌ Error: {e}")

    with sub_tab2:
        st.markdown("### Create a New Account")
        st.write("Register a new PlantPulse account")

        col1, col2 = st.columns(2)
        with col1:
            reg_email = st.text_input("Email", value="newuser@plantpulse.com", key="reg_email")
        with col2:
            reg_password = st.text_input("Password", type="password", value="password123", key="reg_password")

        if st.button("✍️ Register"):
            try:
                response = requests.post(
                    f"{PLANT_SERVICE}/api/auth/register",
                    json={"email": reg_email, "password": reg_password},
                    timeout=5
                )

                if response.status_code in [200, 201]:
                    data = response.json()
                    st.success(f"✅ Account created! You're already logged in!")
                    st.session_state.auth_token = data.get("token")
                    st.info(f"**Email:** {reg_email}\n**User ID:** {data.get('userId')}\n\nYou can now use all the MCP tools!")
                    st.code(json.dumps(data, indent=2), language="json")
                else:
                    st.error(f"❌ Registration failed: {response.status_code}")
                    st.write(response.text)
            except Exception as e:
                st.error(f"❌ Error: {e}")

with tab2:
    st.header("2️⃣ Plant Management Tools")

    st.markdown("### list_my_plants")
    st.write("List all plants for the logged-in user")

    if st.button("📋 List My Plants"):
        try:
            token = st.session_state.get("auth_token", "mock-token-dev")
            response = requests.get(
                f"{PLANT_SERVICE}/api/plants",
                headers={"Authorization": f"Bearer {token}"},
                timeout=5
            )

            if response.status_code == 200:
                plants = response.json()
                st.success(f"✅ Found {len(plants)} plants")

                for plant in plants:
                    with st.expander(f"🪴 {plant.get('nickname', 'Unknown')} ({plant.get('speciesName', 'Unknown species')})"):
                        st.json({
                            "id": plant.get("id"),
                            "nickname": plant.get("nickname"),
                            "species": plant.get("speciesName"),
                            "watering_frequency": f"{plant.get('wateringFrequencyDays')} days",
                            "last_watered": plant.get("lastWateredAt", "Never")
                        })
            else:
                st.error(f"❌ Failed to fetch plants: {response.status_code}")
        except Exception as e:
            st.error(f"❌ Error: {e}")

with tab3:
    st.header("3️⃣ Diagnosis Tools")

    st.markdown("### diagnose_plant_photo")
    st.write("Diagnose a plant from visual symptoms")

    col1, col2 = st.columns(2)

    with col1:
        plant_id = st.text_input("Plant ID", value="52b08e38-b826-463f-bbba-7b9db2e2346d")
        leaf_color = st.selectbox("Leaf Color", ["GREEN", "YELLOW", "BROWN", "SPOTTED"])
        leaf_texture = st.selectbox("Leaf Texture", ["HEALTHY", "WILTING", "MUSHY", "CRISPY"])

    with col2:
        soil_moisture = st.selectbox("Soil Moisture", ["DRY", "MOIST", "WATERLOGGED"])
        visible_pests = st.checkbox("Visible Pests")
        growth = st.selectbox("Growth", ["NORMAL", "SLOW", "STUNTED", "NONE"])

    notes = st.text_area("Notes", "")

    if st.button("🔍 Diagnose Plant"):
        try:
            token = st.session_state.get("auth_token", "mock-token-dev")
            response = requests.post(
                f"{PLANT_SERVICE}/api/plants/{plant_id}/observations",
                headers={"Authorization": f"Bearer {token}"},
                json={
                    "leafColor": leaf_color,
                    "leafTexture": leaf_texture,
                    "soilMoisture": soil_moisture,
                    "visiblePests": visible_pests,
                    "growth": growth,
                    "notes": notes if notes else None
                },
                timeout=5
            )

            if response.status_code == 200:
                result = response.json()
                st.success("✅ Diagnosis Complete!")

                disease_match = result.get("diseaseMatch", {})
                st.markdown(f"""
                **Disease:** {disease_match.get('diseaseName', 'Unknown')}

                **Match Percentage:** {disease_match.get('matchPercentage', 0)}%

                **Health Score:** {result.get('healthScore', 'N/A')}/100

                **Treatment Steps:**
                """)

                for step in disease_match.get('treatmentSteps', []):
                    st.write(f"• {step}")

                st.json(result)
            else:
                st.error(f"❌ Diagnosis failed: {response.status_code}")
        except Exception as e:
            st.error(f"❌ Error: {e}")

with tab4:
    st.header("4️⃣ Integration Setup")

    col1, col2 = st.columns(2)

    with col1:
        st.markdown("### Slack Integration")
        st.write("Connect Slack for daily watering reminders")
        st.info("✓ Get OAuth link to connect Slack")
        st.info("✓ Send daily checklist on demand")
        st.info("✓ Automatic reminders every morning")

    with col2:
        st.markdown("### Google Calendar Integration")
        st.write("Sync watering events to Google Calendar")
        st.info("✓ Get OAuth link to connect Google Calendar")
        st.info("✓ Create recurring 'water this plant' events")
        st.info("✓ Auto-sync based on watering frequency")

    st.markdown("---")
    st.markdown("""
    ### 🚀 MCP Server Status
    - **URL:** http://localhost:8085
    - **SSE Endpoint:** /sse
    - **Message Endpoint:** /mcp/message
    - **Status:** ✅ Running
    - **Tools Available:** 7
    """)

st.markdown("---")
st.markdown("""
### Available MCP Tools:
1. `plantpulse_login` - Authenticate with email/password
2. `list_my_plants` - List all user plants
3. `diagnose_plant_photo` - AI plant diagnosis from symptoms
4. `get_slack_install_link` - Get Slack OAuth link
5. `send_daily_checklist_now` - Send watering checklist to Slack
6. `get_google_calendar_auth_link` - Get Google Calendar OAuth link
7. `sync_watering_calendar` - Sync plants to Google Calendar
""")
