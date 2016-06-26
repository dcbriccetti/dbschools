from django.contrib import admin
from .models import *


class StudentInline(admin.TabularInline):
    model = Student
    extra = 0


class StudentsInSectionInline(admin.TabularInline):
    model = Student.sections.through


class ParentAdmin(admin.ModelAdmin):
    ordering = ('name',)
    list_display = ('name', 'email', 'notes')
    inlines = [StudentInline]


class StudentAdmin(admin.ModelAdmin):
    list_display = ('name', 'active', 'notes', 'list_sections')
    ordering = ('name',)


class SectionAdmin(admin.ModelAdmin):
    list_display = ('course', 'start_time', 'duration_per_day',
                    'num_days', 'price', 'enrolled', 'notes')
    ordering = ('start_time',)
    inlines = [StudentsInSectionInline]


class SectionInline(admin.TabularInline):
    model = Section
    extra = 0


class CourseAdmin(admin.ModelAdmin):
    list_display = ('name', 'active', 'url')
    ordering = ('name',)
    inlines = (SectionInline,)

admin.site.register(Course,  CourseAdmin)
admin.site.register(Section, SectionAdmin)
admin.site.register(Parent,  ParentAdmin)
admin.site.register(Student, StudentAdmin)
