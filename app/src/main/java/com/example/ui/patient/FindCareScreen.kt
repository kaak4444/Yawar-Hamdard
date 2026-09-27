package com.example.ui.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DoctorEntity
import com.example.data.local.FacilityEntity
import com.example.data.model.AppLanguage
import com.example.data.model.ServiceItem
import com.example.ui.common.AppStrings
import com.example.ui.common.HospitalLogoBadge
import com.example.ui.theme.BorderColor
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.DeepGreen
import com.example.ui.theme.Ink
import com.example.ui.theme.PaleBlue
import com.example.ui.theme.PaleGreen
import com.example.ui.theme.Slate
import com.example.ui.theme.YawarBlue
import com.example.ui.theme.YawarNavy
import com.example.ui.theme.DangerBg
import com.example.ui.theme.DangerText
import com.example.ui.viewmodel.YawarViewModel

@Composable
fun FindCareScreen(
    viewModel: YawarViewModel,
    initialTab: Int = 0,
    onOpenBooking: (doctor: DoctorEntity?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val language by viewModel.currentLanguage.collectAsState()
    val doctors by viewModel.filteredDoctors.collectAsState()
    val facilities by viewModel.filteredFacilities.collectAsState()
    val services = viewModel.services
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedProvince by viewModel.selectedProvince.collectAsState()
    val selectedSpecialty by viewModel.selectedSpecialtyFilter.collectAsState()

    val provinces = listOf(
        "All Provinces", "Kabul", "Herat", "Balkh", "Kandahar", "Nangarhar",
        "Khost", "Nimroz", "Badakhshan", "Baghlan", "Bamyan", "Ghazni",
        "Helmand", "Kunduz", "Takhar", "Wardak", "Zabul"
    )
    val specialties = listOf(
        "All", "Sonology", "Neurosurgery & Spine", "Pediatrics", "Ophthalmology",
        "Internal Medicine", "General Surgery", "Cardiology", "Orthopedics & Trauma", "Obstetrics & Gynecology"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row: Doctors | Hospitals | Services
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = YawarNavy,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = ClinicalGreen
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "Doctors (${doctors.size})",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) YawarNavy else Slate
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Hospitals (${facilities.size})",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) YawarNavy else Slate
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        text = "Services (${services.size})",
                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 2) YawarNavy else Slate
                    )
                }
            )
        }

        // Search Bar & Filter Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        text = AppStrings.getSearchPlaceholder(language),
                        color = Slate,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = YawarNavy
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = ClinicalGreen,
                    unfocusedBorderColor = BorderColor
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Province Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(provinces) { prov ->
                    FilterChip(
                        selected = selectedProvince == prov,
                        onClick = { viewModel.setProvince(prov) },
                        label = { Text(prov, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PaleBlue,
                            selectedLabelColor = YawarNavy
                        )
                    )
                }
            }

            // Specialty Filter (visible when Doctors tab selected)
            if (selectedTab == 0) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(specialties) { spec ->
                        FilterChip(
                            selected = selectedSpecialty == spec,
                            onClick = { viewModel.setSpecialtyFilter(spec) },
                            label = { Text(spec, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PaleGreen,
                                selectedLabelColor = DeepGreen
                            )
                        )
                    }
                }
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Doctors Tab
                    if (doctors.isEmpty()) {
                        EmptyFilterView(message = "No doctors match the selected filters or search query.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(doctors) { doc ->
                                DoctorListCard(
                                    doctor = doc,
                                    language = language,
                                    onBook = {
                                        viewModel.startBooking(doc)
                                        onOpenBooking(doc)
                                    },
                                    onClick = { viewModel.openDoctorDetail(doc) }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // Hospitals Tab
                    if (facilities.isEmpty()) {
                        EmptyFilterView(message = "No hospital facilities found for this region.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(facilities) { fac ->
                                HospitalCardDetailed(
                                    facility = fac,
                                    language = language,
                                    onClick = { viewModel.openFacilityDetail(fac) }
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // YHCS Service Catalogue Tab
                    LazyColumn(
                        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(services) { service ->
                            ServiceCardDetailed(
                                service = service,
                                language = language,
                                onClick = { viewModel.openServiceDetail(service) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyFilterView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "No Results",
                tint = Slate,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                color = Slate,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun HospitalCardDetailed(
    facility: FacilityEntity,
    language: AppLanguage,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HospitalLogoBadge(
                    hospitalName = facility.name,
                    size = 48.dp,
                    logoUrl = facility.logoFile,
                    hospitalId = facility.id
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = facility.name,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${facility.facilityType} • ${facility.district}, ${facility.province}",
                        color = YawarBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (facility.hasEmergency24h) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DangerBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "24/7 ER",
                            color = DangerText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = facility.address,
                color = Slate,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Departments summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Departments: ",
                    color = Ink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = facility.departments.take(3).joinToString(", ") + if (facility.departments.size > 3) " +${facility.departments.size - 3} more" else "",
                    color = Slate,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Contact",
                        tint = DeepGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = facility.contactPhone.substringBefore("/").trim(),
                        color = DeepGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                val locationOrDist = if (facility.distanceKm > 0.0) "Approx. ${facility.distanceKm} km away" else "${facility.district}, ${facility.province}"
                Text(
                    text = locationOrDist,
                    color = Slate,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun ServiceCardDetailed(
    service: ServiceItem,
    language: AppLanguage,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(PaleGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = service.titleEn,
                        tint = DeepGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = when (language) {
                            AppLanguage.ENGLISH -> service.titleEn
                            AppLanguage.DARI -> service.titleFa
                            AppLanguage.PASHTO -> service.titlePs
                        },
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "YHCS Core Service Pathway",
                        color = YawarBlue,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = service.oneSentencePromise,
                color = Ink,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = service.description,
                color = Slate,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Who it's for: ${service.forWhom}",
                    color = Slate,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "View Pathway & Documents →",
                    color = ClinicalGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
