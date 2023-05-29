<template>
  <v-container
      id="dashboard"
      fluid
      tag="section"
  >
    <v-row class="text-right">
      <v-spacer></v-spacer>
      <v-col cols="6" md="2">
        <v-btn
            color="success"
            @click="downloadAudit( )"
        >
          Download
          <v-icon>
            mdi-download
          </v-icon>
        </v-btn>
      </v-col>
      <v-col cols="6" md="2">
        <v-btn
            color="success"
            @click="showUploadFileDialog=true"
        >
          Update transcript
          <v-icon>
            mdi-account-reactivate
          </v-icon>
        </v-btn>
      </v-col>
    </v-row>
    <v-row>
      <v-col
          cols="12"
          sm="6"
      >
        <base-material-card
            color="info"
            icon="mdi-account-details"
            title="Student information"
        >
          <v-card-text>
            <div class="display-2 font-weight-light">
              {{ student.name }}
            </div>

            <div class="subtitle-1 font-weight-light">
              {{ student.id }}
            </div>

            <div class="text--primary font-weight-light">
              {{ student.major }}
            </div>

            <div class="text--primary font-weight-light">
              {{ student.admissionSemester }}
            </div>
          </v-card-text>
        </base-material-card>
      </v-col>
      <v-col
          cols="12"
          sm="6"
      >
        <base-material-card
            color="info"
            class="px-5 py-3"
            icon="mdi-book-education"
            title="Academic standing"
        >
          <v-card-text>
            <v-simple-table dense>
              <tbody>
              <tr>
                <td>Cumulative GPA</td>
                <td>{{ student.gpa }}</td>
              </tr>
              <tr>
                <td>Credits Enrolled</td>
                <td>{{ student.creditsEnrolledTotal }}</td>
              </tr>
              <tr>
                <td>Credits Earned</td>
                <td>{{ student.creditsEarnedTotal }}</td>
              </tr>
              </tbody>
            </v-simple-table>
          </v-card-text>
        </base-material-card>
      </v-col>
    </v-row>
    <v-row>
      <v-col
          cols="12"
      >
        <base-material-chart-card
            :data="semesterGPAchart.data"
            :options="semesterGPAchart.options"
            color="success"
            hover-reveal
            type="Line"
        >

          <h4 class="card-title font-weight-light mt-2 ml-2">
            Average GPA over terms
          </h4>

          <p class="d-inline-flex font-weight-light ml-2 mt-1">

          </p>
        </base-material-chart-card>
      </v-col>
    </v-row>
    <v-row>
      <v-select v-model="selectedCurriculum"
                :items="curriculums"
                item-value="id"
                prepend-icon="mdi-format-align-justify"
                menu-props="auto"
                hide-details
                label="Select a curriculum to build a report"
                single-line
      >
        <template slot="selection" slot-scope="data">
          {{ data.item.major }} {{ data.item.year }}
        </template>
        <template slot="item" slot-scope="data">
          {{ data.item.major }} {{ data.item.year }}
        </template>
      </v-select>
      <v-btn
          color="success"
          @click="buildReport()">
        Create
      </v-btn>
    </v-row>

<!--Provide a reminder tool that will, depending on the term of the student, display which classes should the student take to be on time.-->
    <v-row>
      <v-col
          cols="12"
          md="6"
      >
        <base-material-card
            v-if="tableInfo.unmappedRequirements && tableInfo.unmappedRequirements.length > 0"
            icon="mdi-bell-plus"
            :title="'Should take in ' + currentSemesterString"
            class="px-5 py-3"
        >
          <v-simple-table>
            <thead>
            <tr>
              <th></th>
              <th class="primary--text display-1">Course Name</th>
              <th class="primary--text display-1">Credits</th>
              <th class="primary--text display-1">Semester</th>
            </tr>
            </thead>
            <tbody>
            <template v-for="req in tableInfo.unmappedRequirements">
            <!--            <template v-for="req in orderedUnmappedReq">-->
<!--            <template v-for="req in currentSemesterReq">-->
<!--                :key="req.id"-->
<!--                @click.stop="allRowCheckbox(unmappedReq, req)">-->
<!--              <td><input type="checkbox" :value=req v-model="unmappedReq"-->
<!--                         :disabled="mappingReqDisabled"/></td>-->
<!--              <tr :key="req.id">-->
              <tr v-if="req.semester == student.currentSemester" :key="req.id">
                <td></td>
                <td>
                  {{ req.name }}
                </td>
                <td>
                  {{ req.credit }}
                </td>
                <td>
                  {{ req.semester }}
                </td>
              </tr>
            </template>
            </tbody>
          </v-simple-table>
        </base-material-card>
      </v-col>
      <v-col
          cols="12"
          md="6"
      >
        <base-material-card
            v-if="tableInfo.unmappedRequirements && tableInfo.unmappedRequirements.length > 0"
            icon="mdi-bell-alert"
            color="red"
            :title="'Should have already taken before ' + currentSemesterString"
            class="px-5 py-3"
        >
          <v-simple-table>
            <thead>
            <tr>
              <th></th>
              <th class="primary--text display-1">Course Name</th>
              <th class="primary--text display-1">Credits</th>
              <th class="primary--text display-1">Semester</th>
            </tr>
            </thead>
            <tbody>
<!--            <template v-for="req in orderedUnmappedReq">-->
              <template v-for="req in tableInfo.unmappedRequirements">
<!--                  :key="req.id"-->
<!--                  @click.stop="allRowCheckbox(unmappedReq, req)">-->
  <!--              <td><input type="checkbox" :value=req v-model="unmappedReq"-->
  <!--                         :disabled="mappingReqDisabled"/></td>-->
                <tr v-if="req.semester < student.currentSemester" :key="req.id">
                  <td></td>
                  <td>
                    {{ req.name }}
                  </td>
                  <td>
                    {{ req.credit }}
                  </td>
                  <td>
                    {{ req.semester }}
                  </td>
                </tr>
              </template>
            </tbody>
          </v-simple-table>
        </base-material-card>
      </v-col>
    </v-row>

    <base-material-card
        v-if="tableInfo.completeRequirements && tableInfo.completeRequirements.length > 0"
        icon="mdi-text-box-check"
        title="Audit"
        class="px-5 py-3"
    >
      <v-simple-table>
        <thead>
        <tr>
          <th><input class="mr-3" type="checkbox" @click="selectAll" v-model="allSelected"
                     :disabled="unmappingDisabled"/></th>
          <th class="primary--text display-1">Required Course</th>
          <th class="primary--text display-1">Credits</th>
          <th class="primary--text display-1">Taken</th>
          <th class="primary--text display-1">Credits</th>
          <th class="primary--text display-1">Grade Points</th>
          <th class="primary--text display-1">Letter Grade</th>
<!--          <th class="primary&#45;&#45;text display-1">Semester</th>-->
        </tr>
        </thead>

        <tbody>
        <tr v-for="course in tableInfo.completeRequirements"
            :key="course.id"
            @click.stop="allRowCheckbox(map2unmap, course.requirement.id)">
          <td><input type="checkbox" :value=course.requirement.id v-model="map2unmap"
                     :disabled="unmappingDisabled"/></td>
          <td>
            {{ course.requirement.name }}
          </td>
          <td>
            {{ course.requirement.credit }}
          </td>
          <td>
            <div v-if="course.course.code === course.requirement.patterns">
              <v-icon
                  color="success"
                  class="mx-1">
                mdi-checkbox-marked-circle-outline
              </v-icon>
            </div>
            <div v-else-if="course.course.code">
              {{ course.course.code }}
            </div>
          </td>
          <td>
            {{ course.course.credits }}
          </td>
          <td>
            {{ course.course.gradePoint }}
          </td>
          <td>
            {{ course.course.letterGrade }}
          </td>
<!--          <td>-->
<!--            {{ course.requirement.semester }}-->
<!--          </td>-->
        </tr>
<!--        <tr>-->
<!--          <td></td>-->
<!--          <td class="font-weight-bold">Total Credits</td>-->
<!--          <td class="font-weight-bold">{{totalCredits}}</td>-->
<!--        </tr>-->
        </tbody>
      </v-simple-table>
    </base-material-card>
    <v-row>
      <v-col
          cols="12"
          md="6"
      >
        <base-material-card
            v-if="tableInfo.unmappedRequirements && tableInfo.unmappedRequirements.length > 0"
            icon="mdi-text-box-remove"
            title="Unmapped requirements"
            class="px-5 py-3"
        >
          <v-simple-table>
            <thead>
            <tr>
              <th></th>
              <th class="primary--text display-1">Required Course</th>
              <th class="primary--text display-1">Credits</th>
              <th class="primary--text display-1">Semester</th>
            </tr>
            </thead>

            <tbody>
<!--            <tr v-for="req in orderedUnmappedReq"-->
            <tr v-for="req in tableInfo.unmappedRequirements"
                :key="req.id"
                @click.stop="allRowCheckbox(unmappedReq, req)">
              <td><input type="checkbox" :value=req v-model="unmappedReq"
                         :disabled="mappingReqDisabled"/></td>
              <td>
                {{ req.name }}
              </td>
              <td>
                {{ req.credit }}
              </td>
              <td>
                {{ req.semester }}
              </td>
            </tr>
            </tbody>
          </v-simple-table>
        </base-material-card>
      </v-col>
      <v-col
          cols="12"
          md="6"
      >
        <base-material-card
            v-if="tableInfo.unmappedCourses && tableInfo.unmappedCourses.length > 0"
            icon="mdi-text-box-plus"
            title="Unmapped courses"
            class="px-5 py-3"
        >
          <v-simple-table>
            <thead>
            <tr>
              <th></th>
              <th class="primary--text display-1">Taken Course</th>
              <th class="primary--text display-1">Credits</th>
              <th class="primary--text display-1">Grade Pts</th>
              <th class="primary--text display-1">Letter Grade</th>
            </tr>
            </thead>

            <tbody>
            <tr v-for="course in tableInfo.unmappedCourses"
                :key="course"
                @click.stop="allRowCheckbox(unmappedCourse, course)">
              <td><input type="checkbox" :value=course v-model="unmappedCourse"
                         :disabled="mappingCourseDisabled"/>
              </td>
              <td>
                {{ course.code }}
              </td>
              <td>
                {{ course.credits }}
              </td>
              <td>
                {{ course.gradePoint }}
              </td>
              <td>
                {{ course.letterGrade }}
              </td>
            </tr>
            </tbody>
          </v-simple-table>
        </base-material-card>
      </v-col>
    </v-row>

    <v-row>
      <v-col
        cols="12"
        md="6"
      >
        <base-material-card
            v-if="tableInfo.failedCourses && tableInfo.failedCourses.length > 0"
            icon="mdi-text-box-plus"
            title="Failed courses"
            class="px-5 py-3"
        >
          <v-simple-table>
            <thead>
              <tr>
                <th class="primary--text display-1">Taken course</th>
                <th class="primary--text display-1">Credits</th>
                <th class="primary--text display-1">Letter grade</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="course in tableInfo.failedCourses"
                  :key="course"
                  @click.stop="allRowCheckbox(unmappedCourse, course)">
                <td>
                  {{ course.code }}
                </td>
                <td>
                  {{ course.credits }}
                </td>
                <td>
                  {{ course.letterGrade }}
                </td>
              </tr>
            </tbody>
          </v-simple-table>
        </base-material-card>

      </v-col>
    </v-row>

    <v-row class="text-right">
      <v-spacer></v-spacer>
      <v-col cols="3" md="2">
        <v-btn
            v-if="map2unmap.length > 0 || (unmappedCourse.length !== 0 && unmappedReq.length !== 0)"
            color="error"
            @click="dialog = true"
        >
          Map/Unmap
          <v-icon>
            mdi-link
          </v-icon>
        </v-btn>
      </v-col>
      <v-col cols="3" md="2">
        <v-btn
            v-if="tableInfo !== ''"
            color="error"
            @click="del=true, dialog = true"
        >
          Clear
          <v-icon>
            mdi-delete
          </v-icon>
        </v-btn>
      </v-col>
    </v-row>

    <v-dialog
        v-model="showUploadFileDialog"
        max-width="600px"
    >
      <v-card>
        <v-card-title class="primary--text display-2">
          Add transcripts
          <v-spacer />

          <v-icon
              aria-label="Close"
              @click="showUploadFileDialog=false"
          >
            mdi-close
          </v-icon>
        </v-card-title>

        <v-card-text>
          <v-file-input
              v-model="files"
              multiple
              show-size
              counter
              placeholder="Select transcripts"
              prepend-icon="mdi-paperclip"
              outlined
              accept=".pdf"
          >
          </v-file-input>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
              color="primary"
              text
              @click="submitFiles()"
          >
            Ok
          </v-btn>
          <v-btn
              color="primary"
              text
              @click="showUploadFileDialog=false, files = []"
          >
            Close
          </v-btn>
        </v-card-actions>

      </v-card>
    </v-dialog>


    <v-dialog
        v-model="dialog"
        max-width="600"
    >
      <v-card>
        <v-card-title class="warning--text display-2">
          Warning!

          <v-spacer/>

          <v-icon
              aria-label="Close"
              @click="dialog = false, del = false"
          >
            mdi-close
          </v-icon>
        </v-card-title>

        <v-card-text class="text-center">
          {{
            del ? 'Are you sure you want to delete?' : (map2unmap.length > 0 ? 'Are you sure you want to unmap the selected row(s)?' : (unmappedReq.length !== 0 && unmappedCourse.length !== 0 ? 'Are you sure you want to map the selected courses to the selected requirements?' : 'Nothing selected or incorrect selection!'))
          }}
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
              color="warning"
              text
              @click="dialog = false, del = false, map2unmap = [], unmappedCourse = [], unmappedReq = []"
          >
            Cancel
          </v-btn>

          <v-btn
              color="warning"
              text
              @click="del === true ? removeAudit() : mapUnmap(), dialog=false, del=false"
          >
            Yes
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script>
import {get, post, del, download} from '../../helpers/api'
import _ from "lodash";
// import debounce from 'lodash/debounce';

export default {
  name: 'StudentAudit',
  props: ['id'],

  data() {
    return {
      taken: 0,
      earned: 0,
      student: {},
      semesterGPAchart: {},
      tableInfo: '',
      curriculums: [],
      selectedCurriculum: "",
      map2unmap: [],
      unmappingDisabled: false,
      allSelected: false,
      unmappedCourse: [],
      mappingCourseDisabled: false,
      unmappedReq: [],
      mappingReqDisabled: false,
      dialog: false,
      del: false,
      addFiles: false,
      showUploadFileDialog: false
    }
  },
  computed: {
    currentSemesterString: function() {
      var s = this.student;
      var admSem = s.admissionSemester;
      var curSem = s.currentSemester;
      var admSemYear = admSem.substr(admSem.length - 4);
      var curSemYear = parseInt(admSemYear) + parseInt(curSem / 2);
      return (curSem % 2 == 0) ? "Spring " : "Fall " + curSemYear;
    },
    // currentSemesterReq() {
    //   return this.unmappedReq.filter(function(req) {
    //     return req.semester === this.student.currentSemester;
    //     // return true;
    //   })
    // },
    orderedUnmappedReq: function() {
      return _.orderBy(this.unmappedReq, 'semester')
      // return _.sortBy(this.unmappedReq, 'semester')
    }
  },
  watch: {
    map2unmap(val) {
      this.allSelected = val.length === this.tableInfo.completeRequirements.length;
      this.mappingCourseDisabled = this.map2unmap.length > 0;
      this.mappingReqDisabled = this.map2unmap.length > 0;
    },
    unmappedReq() {
      this.unmappingDisabled = this.unmappedReq.length > 0;
      this.mappingCourseDisabled = this.unmappedCourse.length > this.unmappedReq.length + 1;
      this.mappingReqDisabled = this.unmappedCourse.length + 1 < this.unmappedReq.length;
    },
    unmappedCourse() {
      this.unmappingDisabled = this.unmappedCourse.length > 0;
      this.mappingCourseDisabled = this.unmappedCourse.length > this.unmappedReq.length + 1;
      this.mappingReqDisabled = this.unmappedCourse.length + 1 < this.unmappedReq.length;
    },
  },

  methods: {
    submitFiles() {
      if ( this.files.length != 0 ) {
        for(var i = 0; i < this.files.length; i ++ ) {
          let formData = new FormData();
          formData.append('file', this.files[i]);

          post(this, '/transcript', formData, () => {
            this.$store.dispatch('setSnackbar', {
              text: "Transcript uploaded",
            })
          }, error =>{
            this.$store.dispatch('setSnackbar', {
              text: error.response ? error.response.data.message : error,
              color: "error"
            })
          }, {
            'Content-Type': 'multipart/form-data'
          });
        }
        this.showUploadFileDialog = false;
        this.files = [];
      } else {
        console.log("There are no files.");
      }
    },
    getCurriculums() {
      get(this, '/curriculum', '', response => {
        this.curriculums = response.data;
      })
    },
    getInfo() {
      get(this, '/transcript/student/' + this.$route.params.id, '', response => {
        this.student = response.data;
      })
    },
    getSemesterChart() {
      this.semesterGPAchart = {
        data: {
          labels: [],
          series: [[]],
        },
        options: {
          lineSmooth: this.$chartist.Interpolation.simple({
            fillHoles: true
          }),
          low: 0,
          high: 4.5,
          chartPadding: {
            top: 0,
            right: 0,
            bottom: 0,
            left: 0,
          },
        },
      }

      get(this, '/transcript/student/' + this.$route.params.id, '', response => {
        var chartInfo = response.data.studentTerms;
        chartInfo.forEach(x => this.semesterGPAchart.data.labels.push(x.name))
        chartInfo.forEach(x => this.semesterGPAchart.data.series[0].push(x.termGpa))
      })
    },
    getReport() {
      get(this, '/report/' + this.$route.params.id, '', response => {
        this.tableInfo = response.data;
      });
    },
    buildReport() {
      let data = {
        curriculumId: this.selectedCurriculum,
        studentId: this.student.id
      }

      post(this, '/report', data, response => {
        this.tableInfo = response.data;
        this.$store.dispatch('setSnackbar', {text: "Success"})
      }, error => {
        this.$store.dispatch('setSnackbar', {text: error, color: "error"});
      });
    },
    selectAll() {
      this.map2unmap = []
      if (!this.allSelected) {
        this.allSelected = true
        this.tableInfo.completeRequirements.forEach(x => this.map2unmap.push(x.requirement.id))
      } else {
        this.allSelected = false
      }
    },
    mapUnmap() {
      if (this.unmappedReq.length > 0 && this.unmappedCourse.length > 0) {
        for (let i = 0; i < this.unmappedCourse.length; i++) {
          if (this.unmappedReq[i].credit > this.unmappedCourse[i].credits) {
            this.$store.dispatch('setSnackbar', {
              text: `Not enough credits, required: ${this.unmappedReq[i].credit}, provided ${this.unmappedCourse[i].credits}`,
              color: "error"
            });
            return;
          }
        }
        let query = `requirementIds=${this.unmappedReq.map(x => x.id).toString()}&courseIds=${this.unmappedCourse.map(x => x.id).toString()}`;
        console.log(query)
        post(this, '/report/' + this.student.id + '/mapRequirement?' + query, '',
            () => {
              this.getReport();
              this.$store.dispatch('setSnackbar', {text: "Success"});
            }, error => {
              this.$store.dispatch('setSnackbar', {text: error, color: "error"});
            });
      } else {
        post(this, `/report/${this.student.id}/detachRequirements?requirementIds=${this.map2unmap.toString()}`,
            '', () => {
              this.getReport();
              this.$store.dispatch('setSnackbar', {text: "Success"});
            }, error => {
              this.$store.dispatch('setSnackbar', {text: error, color: "error"});
            })
      }
      this.unmappedReq = [];
      this.unmappedCourse = [];
      this.map2unmap = []
    },
    allRowCheckbox(arr, obj) {
      if ((arr === this.map2unmap && !this.unmappingDisabled) ||
          (arr === this.unmappedCourse && !this.mappingCourseDisabled) ||
          (arr === this.unmappedReq && !this.mappingReqDisabled)) {
        if (arr.includes(obj)) {
          arr = arr.splice(arr.indexOf(obj), 1);
        } else {
          arr.push(obj);
        }
      }
    },
    downloadAudit() {
      let _this = this;

      download(_this, '/report/'+ _this.$route.params.id + '/export', '', response => {
      }, error => {
        _this.$store.dispatch('setSnackbar', {
          text: error,
          color: "error"
        })
      });
    },
    removeAudit() {
      del(this, '/report/' + this.$route.params.id, '', () => {
        this.tableInfo = '';
        this.$store.dispatch('setSnackbar', {text: "Success"});
      }, error => {
        this.$store.dispatch('setSnackbar', {text: error, color: "error"});
      });
    }
  },

  created() {
    this.getInfo()
    this.getCurriculums()
    this.getSemesterChart()
    this.getReport()
  },
}
</script>
